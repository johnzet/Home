#ifndef HEAP_4_H

#define HEAP_4_H





typedef struct A_BLOCK_LINK

{

	struct A_BLOCK_LINK *pxNextFreeBlock;	/*<< The next free block in the list. */

	size_t xBlockSize;						/*<< The size of the free block. */

#ifdef MODIFIED_HEAP_4

	struct A_BLOCK_LINK* pxNextBlock;	    /*<< The next allocated block in the list. */

	struct A_BLOCK_LINK* pxPrevBlock;	    /*<< The previous allocated block in the list. */

    char typeName[32];

#endif

} BlockLink_t;



void assertValidHeapObject(void* heapPtr, char* typeName);




#endif