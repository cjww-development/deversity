/*
 *  Copyright 2018 CJWW Development
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package common

class MissingAccountException(msg: String) extends Exception(msg)
class UpdateFailedException(msg: String) extends Exception(msg)
class AlreadyExistsException(msg: String) extends Exception(msg)

class RegistrationCodeExpiredException(msg: String) extends Exception(msg)
class RegistrationCodeNotFoundException(msg: String) extends Exception(msg)

class EnrolmentsNotFoundException(msg: String) extends Exception(msg)

class DeversityIdNotFoundException(msg: String) extends Exception(msg)
