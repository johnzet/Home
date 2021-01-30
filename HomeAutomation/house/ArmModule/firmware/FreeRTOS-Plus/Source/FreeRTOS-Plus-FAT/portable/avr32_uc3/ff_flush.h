/*
 * FreeRTOS+FAT Labs Build 141211 (C) 2014 Real Time Engineers ltd.
 * This file has not yet been released and is therefore CONFIDENTIAL.
 */

#if	!defined(__FF_FLUSH_H__)

#define	__FF_FLUSH_H__

#ifdef	__cplusplus
extern "C" {
#endif

// HT addition: call FF_FlushCache and in addition call cache_write_flush (see secCache.cpp)
FF_Error_t FF_FlushWrites( FF_IOManager_t *pxIOManager, BaseType_t xForced );

#define	FLUSH_DISABLE	1
#define	FLUSH_ENABLE	0

// HT addition: prevent flushing temporarily FF_StopFlush(pIoMan, true)
FF_Error_t FF_StopFlush( FF_IOManager_t *pxIOManager, BaseType_t xFlag );
#ifdef	__cplusplus
}	// extern "C"
#endif


#endif	// !defined(__FF_FLUSH_H__)
