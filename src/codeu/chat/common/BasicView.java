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

import java.util.Collection;

import codeu.chat.common.ConversationHeader;
import codeu.chat.common.ConversationPayload;
import codeu.chat.common.Message;
import codeu.chat.common.ServerInfo;
import codeu.chat.common.User;
import codeu.chat.util.Uuid;

// BASIC VIEW
//
//   The view component in the Model-View-Controller pattern. This component
//   is used to read information from the model where the model is the current
//   state of the server. Data returned from the view should be treated as
//   read only data as manipulating any data returned from the view may
//   have no effect on the server's state.

public interface BasicView {

  // GET USERS
  //
  //   Return all users whose id is found in the given collection.
  Collection<User> getUsers();

  // GET ALL CONVERSATIONS
  //
  //   Return a summary of each converation.
  Collection<ConversationHeader> getConversations();

  // GET CONVERSATIONS
  //
  //   Return all conversations whose id is found in the given collection.
  Collection<ConversationPayload> getConversationPayloads(Collection<Uuid> ids);

  // GET MESSAGES
  //
  //   Return all messages whose id is found in the given collection.
  Collection<Message> getMessages(Collection<Uuid> ids);

  // GET SERVER INFO
  //
  //   Return information about the server (uptime and server version).
  ServerInfo getInfo();

  // USER STATUS UPDATE
  //
  //   Return the conversations created by and contributed to by the specified user.
  Collection<String> userStatusUpdate(String name, Uuid owner);

  // CONVERSATION STATUS UPDATE
  //
  //   Return how many messages have been added to the specified conversation since the last update.
  int conversationStatusUpdate(String title, Uuid owner);

  // ATTEMPT JOIN CONVERSATION
  //
  //   Return whether or not the current user is in the permissions for a specified conversation.
  int attemptJoinConversation(String title, Uuid currentUser);

  // LIST USERS
  //
  //  Return the users present in the conversation with their permission levels
  //  in a set
  Collection<String> listUsers(Uuid currentConversation);
}
