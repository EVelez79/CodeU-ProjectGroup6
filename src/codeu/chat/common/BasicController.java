// Copyright 2017 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package codeu.chat.common;

import codeu.chat.util.Uuid;

// BASIC CONTROLLER
//
//   The controller component in the Model-View-Controller pattern. This
//   component is used to write information to the model where the model
//   is the current state of the server. Data returned from the controller
//   should be treated as read only data as manipulating any data returned
//   from the controller may have no effect on the server's state.
public interface BasicController {

  // NEW MESSAGE
  //
  //   Create a new message on the server. All parameters must be provided
  //   or else the server won't apply the change. If the operation is
  //   successful, a Message object will be returned representing the full
  //   state of the message on the server.
  Message newMessage(Uuid author, Uuid conversation, String body);

  // NEW USER
  //
  //   Create a new user on the server. All parameters must be provided
  //   or else the server won't apply the change. If the operation is
  //   successful, a User object will be returned representing the full
  //   state of the user on the server. Whether user names can be shared
  //   is undefined.
  User newUser(String name);

  // NEW CONVERSATION
  //
  //  Create a new conversation on the server. All parameters must be
  //  provided or else the server won't apply the change. If the
  //  operation is successful, a Conversation object will be returned
  //  representing the full state of the conversation on the server.
  //  Whether conversations can have the same title is undefined.
  ConversationHeader newConversation(String title, Uuid owner);

  // ADD USER INTEREST
  //
  // Adds a user to the interests of the user that is currently signed
  // in. All parameters must be provided or else the server won't apply
  // the change. An integer will be returned representing whether or not
  // the change took place, with "0" meaning that the change occurred
  // and "-1" meaning that the change did not occur. "-2" will be returned
  // if the specified user does not exist.
  int addUserInterest(String name, Uuid owner);

  // REMOVE USER INTEREST
  //
  // Removes a user from the interests of the user that is currently signed
  // in. All parameters must be provided or else the server won't apply
  // the change. An integer will be returned representing whether or not
  // the change took place, with "0" meaning that the change occurred
  // and "-1" meaning that the change did not occur. "-2" will be returned
  // if the specified user does not exist.
  int removeUserInterest(String name, Uuid owner);

  // ADD CONVERSATION INTEREST
  //
  // Adds a conversation to the interests of the user that is currently signed
  // in. All parameters must be provided or else the server won't apply
  // the change. An integer will be returned representing whether or not
  // the change took place, with "0" meaning that the change occurred
  // and "-1" meaning that the change did not occur. "-2" will be returned
  // if the specified conversation does not exist.
  int addConversationInterest(String title, Uuid owner);

  // REMOVE CONVERSATION INTEREST
  //
  // Removes a conversation from the interests of the user that is currently signed
  // in. All parameters must be provided or else the server won't apply
  // the change. An integer will be returned representing whether or not
  // the change took place, with "0" meaning that the change occurred
  // and "-1" meaning that the change did not occur. "-2" will be returned
  // if the specified conversation does not exist.
  int removeConversationInterest(String title, Uuid owner);

  // ADD USER TO CONVERSATION
  //
  // (WORK IN PROGRESS)
  int addUserToConversation(String name, String title, Uuid currentUser);

  // CHANGE PERMISSION LEVEL
  //
  // Changes the permission level of the specified user in the specified
  // conversation. All parameters must be provided or else the server won't
  // apply the change. An integer will be returned representing whether or not
  // the change took place, with "0" meaning that the change occurred, "-1"
  // meaning that the change did not occur, and "1" will be returned if the
  // user attempting the change does not have sufficient permission.
  // "-2" will be returned if the specified user does not already exist in the
  //permission map of the conversation.
  int changePermissionLevel(String name, String title, int permissionLevel, Uuid currentUser);
}
