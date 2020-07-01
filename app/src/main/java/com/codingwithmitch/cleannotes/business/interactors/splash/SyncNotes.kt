package com.codingwithmitch.cleannotes.business.interactors.splash

import com.codingwithmitch.cleannotes.business.data.cache.CacheResponseHandler
import com.codingwithmitch.cleannotes.business.data.cache.abstraction.NoteCacheDataSource
import com.codingwithmitch.cleannotes.business.data.network.ApiResponseHandler
import com.codingwithmitch.cleannotes.business.data.network.abstraction.NoteNetworkDataSource
import com.codingwithmitch.cleannotes.business.data.util.safeApiCall
import com.codingwithmitch.cleannotes.business.data.util.safeCacheCall
import com.codingwithmitch.cleannotes.business.domain.model.Note
import com.codingwithmitch.cleannotes.business.domain.state.DataState
import com.codingwithmitch.cleannotes.business.domain.util.DateUtil
import com.codingwithmitch.cleannotes.util.printLogD
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SyncNotes(
    private val noteCacheDataSource: NoteCacheDataSource,
    private val noteNetworkDataSource: NoteNetworkDataSource
) {

    suspend fun syncNotes() {

        val cachedNotesList = getCachedNotes()
        syncNetworkNotesWithCachedNotes(ArrayList(cachedNotesList))
    }

    private suspend fun getCachedNotes(): List<Note> {

        val cacheResult = safeCacheCall(IO) {
            noteCacheDataSource.getAllNotes()
        }

        val response = object: CacheResponseHandler<List<Note>, List<Note>>(
            response = cacheResult,
            stateEvent = null
        ) {
            override suspend fun handleSuccess(resultObj: List<Note>): DataState<List<Note>>? {
                return DataState.data(
                    response = null,
                    data = resultObj,
                    stateEvent = null
                )
            }
        }.getResult()

        return response?.data ?: ArrayList()
    }

    // get all notes from network
    // if they do not exist in cache, insert them
    // if they do exist in cache, make sure they are up to date
    // while looping, remove notes from the cachedNotes list. If any remain, it means they
    // should be in the network but aren't. So insert them.
    private suspend fun syncNetworkNotesWithCachedNotes(
        cachedNotes: ArrayList<Note>
    ) = withContext(IO) {

        val networkResult = safeApiCall(IO) {
            noteNetworkDataSource.getAllNotes()
        }

        val response = object: ApiResponseHandler<List<Note>, List<Note>>(
            response = networkResult,
            stateEvent = null
        ) {
            override suspend fun handleSuccess(resultObj: List<Note>): DataState<List<Note>>? {
                return DataState.data(
                    response = null,
                    data = resultObj,
                    stateEvent = null
                )
            }
        }.getResult()

        val noteList = response?.data ?: ArrayList()

        val job = launch {
            for (note in noteList) {
                noteCacheDataSource.searchNoteById(note.id)?.let { cachedNote ->
                    cachedNotes.remove(cachedNote)
                    checkIfCachedNoteRequiresUpdate(cachedNote, note)
                } ?: noteCacheDataSource.insertNote(note)
            }
        }

        // wait
        job.join()

        // insert remaining into network
        for (cachedNote in cachedNotes) {
            safeApiCall(IO) {
                noteNetworkDataSource.insertOrUpdateNote(cachedNote)
            }
        }
    }

    private suspend fun checkIfCachedNoteRequiresUpdate(
        cachedNote: Note,
        networkNote: Note
    ) {

        val cacheUpdatedAt = cachedNote.updatedAt
        val networkUpdatedAt = networkNote.updatedAt

        // update cache (network has newest data)
        if (networkUpdatedAt > cacheUpdatedAt) {
            printLogD("SyncNotes",
                "cacheUpdatedAt: ${cacheUpdatedAt}, " +
                        "networkUpdatedAt: ${networkUpdatedAt}, " +
                        "note: ${cachedNote.title}")

            safeCacheCall(IO) {
                noteCacheDataSource.updateNote(
                    primaryKey = networkNote.id,
                    newTitle = networkNote.title,
                    newBody = networkNote.body
                )
            }
        } else {
            // update network (cache has newest data)
            safeApiCall(IO) {
                noteNetworkDataSource.insertOrUpdateNote(cachedNote)
            }
        }
    }

}