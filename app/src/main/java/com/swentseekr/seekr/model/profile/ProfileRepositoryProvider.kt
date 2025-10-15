package com.swentseekr.seekr.model.profile

import com.swentseekr.seekr.model.author.Author
import com.swentseekr.seekr.ui.profile.Profile
import kotlinx.coroutines.runBlocking

object ProfileRepositoryProvider {
  // var profileRepository: ProfileRepository = ProfileRepositoryFirestore(db = Firebase.firestore)
  var _repository: ProfileRepository =
      ProfileRepositoryLocal().apply {
        val sampleProfiles =
            listOf(
                Profile(
                    uid = "user1",
                    author =
                        Author(
                            pseudonym = "Alice",
                            bio = "Test bio",
                            profilePicture = 0,
                            reviewRate = 4.0,
                            sportRate = 3.5),
                    myHunts = mutableListOf(),
                    doneHunts = mutableListOf(),
                    likedHunts = mutableListOf()),
                Profile(
                    uid = "user2",
                    author =
                        Author(
                            pseudonym = "Bob",
                            bio = "Another bio",
                            profilePicture = 0,
                            reviewRate = 3.0,
                            sportRate = 4.0),
                    myHunts = mutableListOf(),
                    doneHunts = mutableListOf(),
                    likedHunts = mutableListOf()))
        runBlocking { sampleProfiles.forEach { profile -> addProfile(profile) } }
      }
  var repository: ProfileRepository = ProfileRepositoryProvider._repository
}
