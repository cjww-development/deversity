/*
 * Copyright 2018 CJWW Development
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package selectors

import reactivemongo.bson.BSONDocument

object UserAccountSelectors {
  val userIdSelector: String => BSONDocument = userId => BSONDocument(
    "userId" -> userId
  )

  val teacherSelector: (String, String) => BSONDocument = (userId, orgDevId) => BSONDocument(
    "userId"                      -> userId,
    "deversityDetails.schoolDevId" -> orgDevId
  )

  val teacherDetailsSelector: (String, String) => BSONDocument = (teacherDevId, orgDevId) => BSONDocument(
    "enrolments.deversityId"      -> teacherDevId,
    "deversityDetails.schoolDevId" -> orgDevId
  )

  val pendingEnrolmentCountSelector: String => BSONDocument = orgName => BSONDocument(
    "deversityDetails.schoolName"      -> orgName,
    "deversityDetails.role"            -> "teacher",
    "deversityDetails.statusConfirmed" -> "pending"
  )
}
