package com.dbschools.music.events;

public enum TypeCode {
    LOGIN, LOGOUT, SAVE_MUSICAN_MUSIC_GROUP, SAVE_OBJECT, UPDATE_OBJECT, DELETE_OBJECT, REMOVE_FROM_ALL_GROUPS
    // Don't reorder or delete. Integer values are stored in database log table. Only add to the end.
}