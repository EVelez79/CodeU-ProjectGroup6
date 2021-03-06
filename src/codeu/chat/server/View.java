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

package codeu.chat.server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import codeu.chat.common.BasicView;
import codeu.chat.common.ConversationHeader;
import codeu.chat.common.ConversationPayload;
import codeu.chat.common.Message;
import codeu.chat.common.SinglesView;
import codeu.chat.common.ServerInfo;
import codeu.chat.common.User;
import codeu.chat.util.Logger;
import codeu.chat.util.Time;
import codeu.chat.util.Uuid;
import codeu.chat.util.store.StoreAccessor;

public final class View implements BasicView, SinglesView {

  private final static Logger.Log LOG = Logger.newLog(View.class);
  private final static ServerInfo info = new ServerInfo();

  private final Model model;

  private Collection<String> contributions;
  private Message currentMessage;
  private boolean foundMessage;
  private ConversationHeader convoPayloadId;

  public View(Model model) {
    this.model = model;
  }


  @Override
  public Collection<User> getUsers() {
    return all(model.userById());
  }

  @Override
  public Collection<ConversationHeader> getConversations() {
    return all(model.conversationById());
  }

  @Override
  public Collection<ConversationPayload> getConversationPayloads(Collection<Uuid> ids) {
    return intersect(model.conversationPayloadById(), ids);
  }

  @Override
  public Collection<Message> getMessages(Collection<Uuid> ids) {
    return intersect(model.messageById(), ids);
  }

  @Override
  public User findUser(Uuid id) { return model.userById().first(id); }

  @Override
  public ConversationHeader findConversation(Uuid id) { return model.conversationById().first(id); }

  @Override
  public Message findMessage(Uuid id) { return model.messageById().first(id); }

  @Override
  public ServerInfo getInfo() {
    return info;
  }

  @Override
  public Collection<String> userStatusUpdate(String name, Uuid owner) {

    // Given a specified user's name and the UUID of the user that wants to
    // request a status update on them, return a collection of conversation
    // titles of conversations that the specified user has either added messages
    // to or created since the last time the user requesting a status update has
    // requested a status update on the specified user.

    Collection<String> contributions = new ArrayList<String>();

    final User foundOwner = model.userById().first(owner);
    final User foundUser = model.userByText().first(name);

    // check if the specified user can be found in Model
    if(foundUser != null) {
      // check if the specified user is in the current user's user interests
      if(foundOwner.UserSet.contains(foundUser.id)) {
        // the last time that the current user requested a status update for the specified user
        final Time lastUserUpdate = foundOwner.UserUpdateMap.get(foundUser.id);
        // go through all of the conversations stored in Model
        contributions = searchContributions(lastUserUpdate, foundUser.id);
        // if after going through everything and no contributions are found, add the note to the collection
        if(contributions.isEmpty()) {
          contributions.add("(No recent conversations)");
        }
        // finally, update the time that status update was last requested for the specified user to now
        foundOwner.UserUpdateMap.put(foundUser.id, Time.now());
      } else {
        // if foundUser is not in the current user's interests, add the note to the collection
      }
    }
    // if foundUser is null, return completely empty collection
    return contributions;
  }

  @Override
  public int conversationStatusUpdate(String title, Uuid owner) {

    // Given a specified conversation's title and the UUID of the user that wants
    // to request a status update on it, return a count of messages that where
    // added to the specified conversation since the last time that the user
    // requesting a status update has requested a status update on the
    // specified conversation.

    int newMessages = 0;

    final User foundOwner = model.userById().first(owner);
    final ConversationHeader foundConversation = model.conversationByText().first(title);

    // check if the specified conversation can be found in Model
    if(foundConversation != null) {
      // check if the specified conversation is in the current user's conversation interests
      if(foundOwner.ConvoSet.contains(foundConversation.id)) {
        // the last time that the current user requested a status update for the specified user
        final Time lastConvoUpdate = foundOwner.ConvoUpdateMap.get(foundConversation.id);
        // go through the entire current conversation and count recent messages.
        newMessages = countRecentMessages(lastConvoUpdate, foundConversation.id);
      // finally, update the time that status update was last requested for the specified converation to now
      foundOwner.ConvoUpdateMap.put(foundConversation.id, Time.now());
      } else {
        // return some negative value to specify that conversation is not in interests
        newMessages = -1;
      }
    } else {
      // return some negative value to specify that conversation doesn't exist
      newMessages = -2;
    }
    return newMessages;
  }

  @Override
  public int attemptJoinConversation(String title, Uuid currentUser) {

    // Given a specified conversation's title and the UUID of the user that wants
    // to join it, return whether or not the user's UUID appears in the permissions
    // map of the specified conversation. If the UUID appears, the user will join
    // the conversation. If not, the user will be notified that they are not
    // allowed to join.

    int attemptJoinConversationResponse = -2;

    ConversationHeader foundConversation = model.conversationByText().first(title);
    if(foundConversation.userCategory.containsKey(currentUser)) {
      LOG.info("User " + currentUser.toString() + " successfully joined " + title + ".");
      attemptJoinConversationResponse = 0;
    } else {
      attemptJoinConversationResponse = -1;
      LOG.info("User " + currentUser.toString() + " is not allowed to join " + title + ".");
    }
    return attemptJoinConversationResponse;
  }

  private Collection<String> searchContributions(Time lastUpdate, Uuid searchUser) {

    // Given a time value for the last time that a check for contributions was
    // requested and the UUID of the user to search for recent contributions from,
    // return a collection of the titles of conversations that the user has created
    // or added messages to after the specified time.

    contributions = new ArrayList<String>();
    Collection<ConversationPayload> allConversations = all(model.conversationPayloadById());

    // go through every conversation
    for(ConversationPayload conversationPayload : allConversations) {
      currentMessage = model.messageById().first(conversationPayload.firstMessage);
      foundMessage = false;
      convoPayloadId = model.conversationById().first(conversationPayload.id);

      addToContributions(currentMessage, foundMessage, lastUpdate, searchUser);

      // check if the current conversation wasn't added to the collection after the above loop
      // and if it matches the specified user
      if(!contributions.contains(convoPayloadId.title) && convoPayloadId.owner.equals(searchUser)) {
          // check to see if the conversation was created after the last status update
          if(lastUpdate.compareTo(convoPayloadId.creation) < 0) {
            // add the conversation to the collection and mark it as recently created
            contributions.add(convoPayloadId.title + " (Creator)");
          }
        }
    }
    return contributions;
  }

  private void addToContributions(Message newCurrentMessage, boolean newFoundMessage, Time lastUpdate, Uuid searchUser) {
    // go through every message
    while(currentMessage != null && foundMessage == false) {
      // check for a matching user UUID and a creation time after the last status update
      if(lastUpdate.compareTo(currentMessage.creation) < 0 && currentMessage.author.equals(searchUser)) {
        // add the conversation's title to the collection and break the loop for this conversation
        contributions.add(convoPayloadId.title);
        foundMessage = true;
      }
      currentMessage = model.messageById().first(currentMessage.next);
    }
  }

  private int countRecentMessages(Time lastUpdate, Uuid searchConversation) {

    // Given a time value for the last time that a check for recent messages was
    // requested and the UUID of the conversation to search through, return a count
    // of messages in the specified conversation that were created after the
    // specified time.

    int newMessages = 0;

    final ConversationPayload searchConversationPayload = model.conversationPayloadById().first(searchConversation);
    Message currentMessage = model.messageById().first(searchConversationPayload.firstMessage);

    while(currentMessage != null) {
      if(lastUpdate.compareTo(currentMessage.creation) < 0) {
        newMessages++;
      }
      currentMessage = model.messageById().first(currentMessage.next);
    }
    return newMessages;
  }

  public Collection<String> listUsers(Uuid currentConversation) {
    final ConversationHeader foundConversation = model.conversationById().first(currentConversation);
    Collection<String> userCategorySet = new HashSet<>();

    for (Map.Entry<Uuid, Integer> entry : foundConversation.userCategory.entrySet()) {
      // Convert the key-value pairs to Strings to allow them to be serialized
       String stringEntry = entry.toString();
       userCategorySet.add(stringEntry);
    }

    return userCategorySet;
  }

  private static <S,T> Collection<T> all(StoreAccessor<S,T> store) {

    final Collection<T> all = new ArrayList<>();

    for (final T value : store.all()) {
        all.add(value);
    }

    return all;
  }

  private static <T> Collection<T> intersect(StoreAccessor<Uuid, T> store, Collection<Uuid> ids) {

    // Use a set to hold the found users as this will prevent duplicate ids from
    // yielding duplicates in the result.

    final Collection<T> found = new HashSet<>();

    for (final Uuid id : ids) {

      final T t = store.first(id);

      if (t == null) {
        LOG.warning("Unmapped id %s", id);
      } else if (found.add(t)) {
        // do nothing
      } else {
        LOG.warning("Duplicate id %s", id);
      }
    }

    return found;
  }
}
