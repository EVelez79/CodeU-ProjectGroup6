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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

import codeu.chat.util.Serializer;
import codeu.chat.util.Serializers;
import codeu.chat.util.Time;
import codeu.chat.util.Uuid;
import java.util.Map;
import java.util.HashMap;

public final class ConversationHeader {

  public static final Serializer<ConversationHeader> SERIALIZER = new Serializer<ConversationHeader>() {

    @Override
    public void write(OutputStream out, ConversationHeader value) throws IOException {

      Uuid.SERIALIZER.write(out, value.id);
      Uuid.SERIALIZER.write(out, value.owner);
      Time.SERIALIZER.write(out, value.creation);
      Serializers.STRING.write(out, value.title);

    }

    @Override
    public ConversationHeader read(InputStream in) throws IOException {

      return new ConversationHeader(
          Uuid.SERIALIZER.read(in),
          Uuid.SERIALIZER.read(in),
          Time.SERIALIZER.read(in),
          Serializers.STRING.read(in)
      );

    }

  };

  public enum PermissionLevel{
    member(1),
    owner(2),
    creator(3);

    private int level;
    PermissionLevel(int level){
      this.level = level;
    }

    public int getLevel(){
      return this.level;
    }
  }

  public final Uuid id;
  public final Uuid owner;
  public final Time creation;
  public final String title;
  public Map<Uuid, Integer> userCategory = new HashMap<Uuid, Integer>();

  public ConversationHeader(Uuid id, Uuid owner, Time creation, String title) {

    PermissionLevel permissionLevel = PermissionLevel.creator;
    this.id = id;
    this.owner = owner;
    this.creation = creation;
    this.title = title;
    this.userCategory.put(owner, permissionLevel.getLevel());
  }

  public int userCounter(){
    int count = 1;
    ArrayList<Uuid> users = new ArrayList<Uuid>();
    for(Uuid i: userCategory.keySet()){
      if (!users.contains(i)){
        users.add(i);
      }
      else{
        count += 1;
      }
    }
    return count;
  }
}
