package com.swentseekr.seekr.model.profile

object ProfileRepositoryProvider {
  // var profileRepository: ProfileRepository = ProfileRepositoryFirestore(db = Firebase.firestore)
  var _repository: ProfileRepository = ProfileRepositoryLocal()
  var repository: ProfileRepository = ProfileRepositoryProvider._repository
}
