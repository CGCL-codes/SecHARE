package org.example.dao.nosql.mongo.util;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

import java.util.Set;

public class MongoDaoUtil {

  private static final BiMap<Character, Character> RESERVED_CHARACTERS = HashBiMap.create();

  static {
    RESERVED_CHARACTERS.put('.', (char) 0xFF0E);
    RESERVED_CHARACTERS.put('$', (char) 0xFF04);
  }

  private MongoDaoUtil() {
  }

  /**
   * Specific method for recursive substitute the reserved $ and . characters
   * in the key names of the DBObject.
   *
   * @param profileBody the profileBody
   * @return encoded DBObject
   */
  public static DBObject encodeReservedCharacters(DBObject profileBody) {
    if (profileBody == null) {
      return null;
    }
    if (profileBody instanceof BasicDBList) {
      BasicDBList dbList = (BasicDBList) profileBody;
      BasicDBList modifiedList = new BasicDBList();
      for (Object value : dbList) {
        if (value instanceof DBObject) {
          modifiedList.add(encodeReservedCharacters((DBObject) value));
        } else {
          modifiedList.add(value);
        }
      }
      return modifiedList;
    } else {
      Set<String> keySet = profileBody.keySet();
      DBObject modifiedNode = new BasicDBObject();
      if (keySet != null) {
        for (String key : keySet) {
          Object value = profileBody.get(key);
          for (char symbolToReplace : RESERVED_CHARACTERS.keySet()) {
            key = key.replace(symbolToReplace, RESERVED_CHARACTERS.get(symbolToReplace));
          }
          if (value instanceof DBObject) {
            modifiedNode.put(key, encodeReservedCharacters((DBObject) value));
          } else {
            modifiedNode.put(key, value);
          }
        }
      }
      return modifiedNode;
    }
  }

  /**
   * Specific method for recursive decoding the reserved $ and . characters in the key names of the
   * DBObject.
   *
   * @param profileBody the profileBody
   * @return decoded DBObject
   */
  public static DBObject decodeReservedCharacters(DBObject profileBody) {

    if (profileBody == null) {
      return null;
    }
    if (profileBody instanceof BasicDBList) {
      BasicDBList dbList = (BasicDBList) profileBody;
      BasicDBList modifiedList = new BasicDBList();
      for (Object value : dbList) {
        if (value instanceof DBObject) {
          modifiedList.add(decodeReservedCharacters((DBObject) value));
        } else {
          modifiedList.add(value);
        }
      }
      return modifiedList;
    } else {
      Set<String> keySet = profileBody.keySet();
      DBObject modifiedNode = new BasicDBObject();
      if (keySet != null) {
        for (String key : keySet) {
          Object value = profileBody.get(key);
          for (char symbolToReplace : RESERVED_CHARACTERS.values()) {
            key = key.replace(symbolToReplace, RESERVED_CHARACTERS.inverse().get(symbolToReplace));
          }
          if (value instanceof DBObject) {
            modifiedNode.put(key, decodeReservedCharacters((DBObject) value));
          } else {
            modifiedNode.put(key, value);
          }
        }
      }
      return modifiedNode;
    }
  }
}
