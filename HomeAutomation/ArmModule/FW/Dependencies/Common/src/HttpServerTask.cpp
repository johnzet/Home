#include <HttpServerTask.h>

HttpServerTask::HttpServerTask(QueueHandle_t httpRequestQueue, WiFiReceiverTask* wiFiReceiverTaskArg, WiFiTransmitterTask* wiFiTransmitterTaskArg) {
    this->wifiReceiverTask = wiFiReceiverTaskArg;
    this->wiFiTransmitterTask = wiFiTransmitterTaskArg;
    this->ipAddress = 0;
    this->httpRequestQueue = httpRequestQueue;
}

bool HttpServerTask::waitForWiFiAssociation() {
    uint8_t count = 0;
    uint8_t associationStatus = 0xff;
    do {
        AtCmdPacket p = AtCmdPacket();
        p.setAtCmd("AI");
        Zstring cmdPayload = Zstring();
        p.setPayload(&cmdPayload);

        this->wiFiTransmitterTask->sendAtCmd(&p);
        vTaskDelay(2/portTICK_PERIOD_MS);

        AtCmdPacket packet = AtCmdPacket();
        uint8_t loopCnt = 0;
        bool gotResponse = false;
        do {

            gotResponse = this->wifiReceiverTask->getAtCmdResponsePacket(&packet);
            if (!gotResponse) {
                vTaskDelay(10/portTICK_PERIOD_MS);
            }
        } while(!gotResponse && loopCnt++ < 5);
        if (packet.getStatus() == 0) {
            associationStatus = ((uint8_t)packet.getPayload()->getChar(0));
        }
        if (associationStatus != 0) {
            vTaskDelay(1000/portTICK_PERIOD_MS);
        }
    } while(count++ < 20 && associationStatus != 0);


    return (associationStatus == 0);
}

bool HttpServerTask::getIpAddress() {
    uint8_t count = 0;
    this->ipAddress = 0;
    do {
        AtCmdPacket p = AtCmdPacket();
        p.setAtCmd("MY");
        Zstring cmdPayload = Zstring();
        p.setPayload(&cmdPayload);

        this->wiFiTransmitterTask->sendAtCmd(&p);
        vTaskDelay(2/portTICK_PERIOD_MS);

        AtCmdPacket packet = AtCmdPacket();
        uint8_t loopCnt = 0;
        bool gotResponse = false;
        do {

            gotResponse = this->wifiReceiverTask->getAtCmdResponsePacket(&packet);
            if (!gotResponse) {
                vTaskDelay(10/portTICK_PERIOD_MS);
            }
        } while(!gotResponse && loopCnt++ < 5);
        if (packet.getStatus() == 0) {
            this->ipAddress = ((uint8_t)packet.getPayload()->getChar(0)) << 24;
            this->ipAddress += ((uint8_t)packet.getPayload()->getChar(1)) << 16;
            this->ipAddress += ((uint8_t)packet.getPayload()->getChar(2)) << 8;
            this->ipAddress += ((uint8_t)packet.getPayload()->getChar(3));
        } else {
            vTaskDelay(1000/portTICK_PERIOD_MS);
        }
    } while(count++ < 20 && this->ipAddress < (1 << 24));

    
    return (this->ipAddress > (1 << 24));
}

bool HttpServerTask::getTimeFromServerByHttp() {
    HttpPacket packet = HttpPacket();
    packet.setAddress(CONFIG_SERVER_IP_ADDRESS);
//    packet.setDestPort(80);
    packet.setFrameId(01);
    packet.setFrameType(0x20);
    packet.setOptions(0);
    packet.setProtcol(0x01);
    packet.setSourcePort(80);  // used as the dest port

//while(true) {
    wiFiTransmitterTask->sendIpv4TxRequestPacket(&packet, "/house/config");
    HttpPacket* p = new HttpPacket();
    if (xQueueReceive(wifiReceiverTask->getReceivedIpv4PacketQueue(), p, 1000/portTICK_PERIOD_MS) != pdPASS) {
        return false;
    }

    char* payload = p->getPayload()->getStr();
    uint32_t length = p->getPayload()->size();

    payload[length-1] = NULL;   // just in case
    char* token = "\"rtcShortcut\": \"";
    char* loc = strstr(payload, token);

    if (loc != NULL) {
        uint32_t seconds=0;
        uint32_t minutes=0;
        uint32_t hours=0;
        uint32_t day=0;
        uint32_t month=0;
        uint32_t weekdayMondayIs1=0;
        uint32_t yearYY=0;

        int countParsed = sscanf(loc+strlen(token), "%u%u%u%u%u%u%u", &seconds, &minutes, &hours, &day, &month, &weekdayMondayIs1, &yearYY);
        if (countParsed == 7) {
            clock->setClock(seconds, minutes, hours, day, month, weekdayMondayIs1, yearYY);
        }
    } else {
        //delete p;
        return false;
    }
        
    //delete p;
    return true;
}


void HttpServerTask::createHttpResponse(HttpPacket* packet) {


    assert(packet->getPayload() != NULL);
    assert(packet->getPayload()->size() < 2000);

    char* payload = new char[packet->getPayload()->size() +1];
    pvPortSetTypeName(payload, "PayldM1");
    memcpy(payload, packet->getPayload()->getStr(), packet->getPayload()->size());
    payload[packet->getPayload()->size() ] = NULL;

    char* savePtr;

    char* pch = strtok_r(payload, " \t", &savePtr);
    if (strcmp("GET", pch) != 0) {
        return;
    }
    pch = strtok_r(NULL, " \t", &savePtr);
    char* resource = pch;


    if (strcmp("/status/heap", resource) == 0) {
       createHttpHeapStatusResponse(packet);
    } else if (strcmp("/status", resource) == 0) {
       createHttpStatusResponse(packet);
    } else if (strcmp("/display", resource) == 0) {
       createHttpDisplayResponse(packet);
    }

    if (!createSpecializedHttpResponse(packet, resource)) {
       createHttpRootUsageResponse(packet);
    }

    delete[] payload;
}

void HttpServerTask::createHttpStatusResponse(HttpPacket* packet) {
    checkStackLevel();
    this->wiFiTransmitterTask->startChunkedIpv4Response(packet);
    taskYIELD();

    char* buffer = new char[1024];
    pvPortSetTypeName(buffer, "bufferM2");
    Zstring* msg = new Zstring();
    pvPortSetTypeName(msg, "ZstrM1");
    
    msg->appendS("<!doctype html>");
    msg->appendS("<html>");
    msg->appendS("<head><style>table, th, td {border: 1px solid black;}</style></head>");
    msg->appendS("<body>");
    msg->appendS("<h1>ARM module</h1><br/>");

    msg->appendS("Status Reply Message<br/><br/>");
    clock->prettyPrint(buffer);
    msg->appendS(buffer);
    taskYIELD();

    msg->appendS("<br/><br/>Main stack level = ");
    sprintf(buffer, "%.2f%%", STACK_LEVEL);
    msg->appendS(buffer);
    msg->appendS("<br/>Process stack level = ");
    sprintf(buffer, "%.2f%%", PROCESS_STACK_LEVEL);
    msg->appendS(buffer);

//    msg->appendS("<br/><br/>Global remaining heap free size = ");
//    sprintf(buffer, "%i", getRemainingHeapSize());
//    msg->appendS(buffer);

    msg->appendS("<br/><br/>FreeRTOS heap free size = ");
    sprintf(buffer, "%i", xPortGetFreeHeapSize());
    msg->appendS(buffer);

    msg->appendS("<br/><br/>FreeRTOS minimum ever heap free size = ");
    sprintf(buffer, "%i", xPortGetMinimumEverFreeHeapSize());
    msg->appendS(buffer);

    msg->appendS("<br/><br/>HttpServerTask stack high water mark = ");
    sprintf(buffer, "%u", (unsigned int)uxTaskGetStackHighWaterMark(NULL /*this->handle*/));
    msg->appendS(buffer);

    msg->appendS("<br/>");
    this->wiFiTransmitterTask->sendIpv4ResponseChunk(packet, msg);
    msg->clear();
    taskYIELD();



    msg->appendS("<br/><b>Task list:</b><br/><pre>");
    msg->appendS("Name          State   Prio  StckHighWtr #<br/>");
    msg->appendS("-----------------------------------------<br/>");
    vTaskList(buffer);
    msg->appendS(buffer);
    msg->appendS("</pre>");

//    msg->appendS("<b>Runtime stats</b><pre>");
//    xTaskGetRunTimeStats(buffer);
//    msg->appendS(buffer);
//    msg->appendS("</pre>");

    this->wiFiTransmitterTask->sendIpv4ResponseChunk(packet, msg);
    msg->clear();
    taskYIELD();


    msg->appendS("<br/>Messages<br/>");
    msg->appendS("<table>");
    msg->appendS("<tr><th>Date</th><th>Message</th></tr>");
    MessageRecord* mr = messageList->getFirst();
    while(mr != NULL) {
        msg->appendS("<tr><td>");
        clock->prettyPrint(buffer, &mr->date, &mr->time);
        msg->appendS(buffer);
        msg->appendS("</td><td>");
        msg->appendS(mr->text);
        msg->appendS("</td></tr>");
        mr = messageList->getNext(mr);
    }
    msg->appendS("</table>");
    msg->appendS("<br/>");

    msg->appendS("</body>");
    msg->appendS("</html>");

    createSpecializedHttpStatusResponse(packet, msg, buffer);

    this->wiFiTransmitterTask->endChunkedIpv4Response(packet);
    taskYIELD();

    delete msg;
    delete[] buffer;
}

void HttpServerTask::createHttpHeapStatusResponse(HttpPacket* packet) {
    uint32_t heapSTRUCT_SIZE	= ( ( sizeof ( BlockLink_t ) + ( 8 - 1 ) ) & ~0x7 );
    assert(heapSTRUCT_SIZE >= 8 && heapSTRUCT_SIZE < 100);
    assert(packet > (HttpPacket*)0x20000000 && packet < (HttpPacket*)0x20030000);

#ifdef MODIFIED_HEAP_4
    assert(osAllocatedHeapStart > (BlockLink_t*)0x20000000 && osAllocatedHeapStart < (BlockLink_t*)0x20030000);
    BlockLink_t* heapPtr = osAllocatedHeapStart;
    BlockLink_t* heapEndPtr = osAllocatedHeapStart;

    while (heapEndPtr->pxNextBlock != NULL) {
        // Record the end of heap before we stream any more data
        heapEndPtr = heapEndPtr->pxNextBlock;
    }
    assert(heapEndPtr > (BlockLink_t*)0x20000000 && heapEndPtr < (BlockLink_t*)0x20030000);
#endif


    this->wiFiTransmitterTask->startChunkedIpv4Response(packet);
    taskYIELD();

    char* buffer = new char[1024];
    pvPortSetTypeName(buffer, "bufferM3");
    Zstring* msg = new Zstring();
    pvPortSetTypeName(msg, "ZstrM2");
    
    
    msg->appendS("<!doctype html>");
    msg->appendS("<html>");
    msg->appendS("<head><style>table, th, td {border: 1px solid grey;}</style></head>");
    msg->appendS("<body>");
    msg->appendS("<h1>ARM module</h1><br/>");

    msg->appendS("OS Heap Contents<br/><br/>");

    msg->appendS("<br/><br/>FreeRTOS heap free size = ");
    itoa(xPortGetFreeHeapSize(), buffer, 10);
    msg->appendS(buffer);

    msg->appendS("<br/><br/>FreeRTOS minimum ever heap free size = ");
    itoa(xPortGetMinimumEverFreeHeapSize(), buffer, 10);
    msg->appendS(buffer);

    msg->appendS("<br/>");

#ifndef MODIFIED_HEAP_4
    msg->appendS("<br/><br/>***** MODIFIED_HEAP_4 is not defined *****<br/>");
#endif

#ifdef MODIFIED_HEAP_4

    msg->appendS("<br/><br/>OS Heap (0x2000 0000 - 0x2003 0000)<br/>");
    msg->appendS("<table>"); 
    uint32_t totalMemory = 0;

    
    this->wiFiTransmitterTask->sendIpv4ResponseChunk(packet, msg);
    msg->clear();
    taskYIELD();

    /*uint32_t rowCounter = 0;*/
    do {
       
        if ((heapPtr->xBlockSize & ~0x80000000) > 0x30000 
            || !(isValidHeapAddress((uint8_t*)heapPtr->pxNextBlock) || heapPtr->pxNextBlock == 0) 
            || !(isValidHeapAddress((uint8_t*)heapPtr->pxPrevBlock) || heapPtr->pxPrevBlock == 0)
            /*|| strlen(heapPtr->typeName) <= 0 && rowCounter++ > 0*/ ) {
            this->wiFiTransmitterTask->sendIpv4ResponseChunk(packet, msg);
            msg->clear();
            taskYIELD();

            msg->appendS("Memory corruption ~address ");
            itoa((uint32_t)(((uint8_t*)heapPtr) + heapSTRUCT_SIZE), buffer, 16);
            msg->appendS(buffer);

            this->wiFiTransmitterTask->sendIpv4ResponseChunk(packet, msg);
            msg->clear();
            taskYIELD();
            break;
        }


        msg->appendS("<tr>");
        msg->appendS("<td>0x");
        itoa((uint32_t)(((uint8_t*)heapPtr) + heapSTRUCT_SIZE), buffer, 16);
        msg->appendS(buffer);
        msg->appendS("</td>");
        msg->appendS("<td>");
        msg->appendS(heapPtr->typeName);
        msg->appendS("</td>");
        msg->appendS("<td>");
        totalMemory += (heapPtr->xBlockSize & 0x00FFFFFF);
        itoa(heapPtr->xBlockSize & 0x00FFFFFF, buffer, 10);
        msg->appendS(buffer);
        msg->appendS(" bytes");
        msg->appendS("</td></tr>");

        this->wiFiTransmitterTask->sendIpv4ResponseChunk(packet, msg);
        msg->clear();
        taskYIELD();

        if (heapPtr == heapEndPtr) break;
        heapPtr = heapPtr->pxNextBlock;
        assert(heapPtr > (BlockLink_t*)0x20000000 && heapPtr < (BlockLink_t*)0x20030000);

    } while (heapPtr != NULL);
    msg->appendS("</table>");

    msg->appendS("<br/>Total Heap = ");
    itoa(totalMemory, buffer, 10);
    msg->appendS(buffer);
    msg->appendS(" bytes<br/>");

 #endif

    msg->appendS("<br/>");
    msg->appendS("</body>");
    msg->appendS("</html>");


    this->wiFiTransmitterTask->sendIpv4ResponseChunk(packet, msg);
    msg->clear();
    taskYIELD();
    this->wiFiTransmitterTask->endChunkedIpv4Response(packet);
    delete msg;
    delete[] buffer;
}

void HttpServerTask::createHttpDisplayResponse(HttpPacket* packet) {

    assertValidHeapObject(packet, NULL);

    this->wiFiTransmitterTask->startChunkedIpv4Response(packet);
    taskYIELD();

    Zstring* msg = new Zstring();
    pvPortSetTypeName(msg, "ZstrM3");
    
    
    msg->appendS("<!doctype html>");
    msg->appendS("<html>");
    msg->appendS("<head><style>table, th, td {border: 1px solid black;}</style></head>");
    msg->appendS("<body>\n");
   
    msg->appendS("<script type='text/javascript'>\n");


    uint32_t lcdWidth = lcd->getWidth();
    uint32_t lcdHeight = lcd->getHeight();
    msg->appendS("  var margin=10;\n");
    msg->appendS("  var viewportWidth = document.body.clientWidth - 2*margin;\n");
    msg->appendS("  var lcdWidth = "); msg->appendI(static_cast<int>(lcdWidth)); msg->appendS(";\n");
    msg->appendS("  var lcdHeight = "); msg->appendI(static_cast<int>(lcdHeight)); msg->appendS(";\n");
    msg->appendS("  var aspectRatio = lcdWidth/lcdHeight;\n");
    msg->appendS("  var viewportHeight = viewportWidth / aspectRatio;\n");

    msg->appendS("  var svg = document.createElementNS('http://www.w3.org/2000/svg', 'svg');\n");
    msg->appendS("  svg.setAttribute('x', margin);\n");
    msg->appendS("  svg.setAttribute('y', margin);\n");
    msg->appendS("  svg.setAttribute('width', viewportWidth);\n");
    msg->appendS("  svg.setAttribute('height', viewportHeight);\n");
    msg->appendS("  var xform = document.createElementNS('http://www.w3.org/2000/svg', 'g');\n");
    msg->appendS("  xform.setAttribute(\"transform\", \"translate(\" + margin + \",\" + margin + \")scale(\" + (viewportWidth-2*margin)/lcdWidth + \", \" + (viewportHeight-2*margin)/lcdHeight + \")\");\n");
    msg->appendS("  svg.appendChild(xform);\n");

    assertValidHeapObject(packet, NULL);
    this->wiFiTransmitterTask->sendIpv4ResponseChunk(packet, msg);
    msg->clear();
    taskYIELD();

    assertValidHeapObject(msg, "ZstrM3");

    msg->appendS("var data=[");
    uint32_t chunkCounter = 0;
    for (uint32_t column=0; column<lcdWidth; column++) {
        for (uint32_t row=0; row<lcdHeight; row++) {
            uint8_t color = lcd->getPixel(column, row);
            if (color > 0) {
                color = 0xF - color;
                msg->appendI(static_cast<int>(row));
                msg->appendS(",");
                msg->appendI(static_cast<int>(column));
                msg->appendS(",'");
                msg->appendI(color);
                msg->appendS("',");
                
                chunkCounter++;
                if (chunkCounter > 100) {
                    assertValidHeapObject(packet, NULL);
                    this->wiFiTransmitterTask->sendIpv4ResponseChunk(packet, msg);
                    msg->clear();
                    assertValidHeapObject(msg, "ZstrM3");
                    taskYIELD();
                    chunkCounter = 0;
                }
            }
        }
    }
    msg->appendS("-1,-1,-1];\n");

    assertValidHeapObject(packet, NULL);
    this->wiFiTransmitterTask->sendIpv4ResponseChunk(packet, msg);
    msg->clear();
    taskYIELD();

    assertValidHeapObject(msg, "ZstrM3");

    msg->appendS("  var border = document.createElementNS('http://www.w3.org/2000/svg', 'rect');\n");
    msg->appendS("  border.setAttribute('x', 0);\n");
    msg->appendS("  border.setAttribute('y', 0);\n");
    msg->appendS("  border.setAttribute('width', lcdWidth);\n");
    msg->appendS("  border.setAttribute('height', lcdHeight);\n");
    msg->appendS("  border.setAttribute('stroke', '#88C');\n");
    msg->appendS("  border.setAttribute('fill', 'none');\n");
    msg->appendS("  xform.appendChild(border);\n");

    msg->appendS("var i = 0;\n");
    msg->appendS("while(true) {\n");
    msg->appendS("  if (data[i] < 0) break;\n");
    msg->appendS("  var rect = document.createElementNS('http://www.w3.org/2000/svg', 'rect');\n");
    msg->appendS("  rect.setAttribute('y', data[i++]);\n");
    msg->appendS("  rect.setAttribute('x', data[i++]);\n");
    msg->appendS("  rect.setAttribute('width', 1);\n");
    msg->appendS("  rect.setAttribute('height', 1);\n");
    msg->appendS("  rect.setAttribute('stroke', 'none');\n");
    msg->appendS("  rect.setAttribute('fill', '#' + data[i] + data[i] + data[i++]);\n");
    msg->appendS("  xform.appendChild(rect);\n");
    msg->appendS("}\n");
     
    msg->appendS("document.body.appendChild(svg);\n");

    msg->appendS("</script>\n");


    msg->appendS("</body>");
    msg->appendS("</html>");


    this->wiFiTransmitterTask->sendIpv4ResponseChunk(packet, msg);
    msg->clear();
    taskYIELD();
    assertValidHeapObject(packet, NULL);
    this->wiFiTransmitterTask->endChunkedIpv4Response(packet);

    assertValidHeapObject(msg, "ZstrM3");
    delete msg;
}

void HttpServerTask::createHttpRootUsageResponse(HttpPacket* packet) {
    this->wiFiTransmitterTask->startChunkedIpv4Response(packet);
    taskYIELD();
    Zstring* msg = new Zstring();
    pvPortSetTypeName(msg, "ZstrM6");
    
    msg->appendS("<!doctype html>");
    msg->appendS("<html>");
    msg->appendS("<body>");
    msg->appendS("<h2>Usage:</h2>");
    msg->appendS("<ul>");

    msg->appendS("<li>/display - show current LCD screen</li>");
    msg->appendS("<li>/status - debugging information</li>");
    msg->appendS("<li>/status/heap - heap debugging information</li>");
    createSpecializedRootUsageResponse(msg);
    msg->appendS("</ul>");
    msg->appendS("</body></html>");

    this->wiFiTransmitterTask->sendIpv4ResponseChunk(packet, msg);
    msg->clear();
    taskYIELD();

    this->wiFiTransmitterTask->endChunkedIpv4Response(packet);
    delete msg;

}
