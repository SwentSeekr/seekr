package com.swentseekr.seekr.model.hunt

object HuntsImageRepositoryConstantsString {
    const val PATH= "hunts_images"
    const val MAIN= "/main_"
    const val FORMAT= ".jpg"
    const val OTHER = "/other_"
    const val IMAGE = "img"
    const val UNDERSCORE= "_"
    const val TAG = "HuntsImageRepository"
    const val TAG_FIRESTORE= "HuntsRepositoryFirestore"
    const val ERROR_DELETE= "Failed to delete"
    const val ERROR_TIMEOUT= "Timeout listing images for hunt"
    const val ERROR_POSSIBLE= "(likely empty folder)"
    const val ERROR_UNEXPECTED= "Unexpected error deleting images for"
    const val ERROR_DELETING= "Failed to delete image:"
}
object HuntsImageRepositoryConstantsDefault{
    const val TIME_OUT: Long = 2_000
}