package com.codingwithmitch.cleannotes.business.interactors.notelist

import com.codingwithmitch.cleannotes.business.interactors.common.DeleteNote
import com.codingwithmitch.cleannotes.framework.presentation.notedetail.state.NoteDetailViewState

class NoteListInteractors(
    val insertNewNote: InsertNewNote,
    val deleteNote: DeleteNote<NoteDetailViewState>,
    val searchNotes: SearchNotes,
    val getNumNotes: GetNumNotes,
    val restoreDeletedNote: RestoreDeletedNote,
    val deleteMultipleNotes: DeleteMultipleNotes
)