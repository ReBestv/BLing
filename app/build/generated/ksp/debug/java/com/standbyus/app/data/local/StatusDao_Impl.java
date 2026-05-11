package com.standbyus.app.data.local;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class StatusDao_Impl implements StatusDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<StatusEntity> __insertionAdapterOfStatusEntity;

  private final SharedSQLiteStatement __preparedStmtOfClearAll;

  public StatusDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfStatusEntity = new EntityInsertionAdapter<StatusEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `status_cache` (`userId`,`doing`,`customDoing`,`feeling`,`feelingColor`,`feelingEmoji`,`note`,`updatedAt`,`source`) VALUES (?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final StatusEntity entity) {
        statement.bindString(1, entity.getUserId());
        statement.bindString(2, entity.getDoing());
        statement.bindString(3, entity.getCustomDoing());
        statement.bindString(4, entity.getFeeling());
        statement.bindString(5, entity.getFeelingColor());
        statement.bindString(6, entity.getFeelingEmoji());
        statement.bindString(7, entity.getNote());
        statement.bindLong(8, entity.getUpdatedAt());
        statement.bindString(9, entity.getSource());
      }
    };
    this.__preparedStmtOfClearAll = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM status_cache";
        return _query;
      }
    };
  }

  @Override
  public Object upsertStatus(final StatusEntity status,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfStatusEntity.insert(status);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object clearAll(final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfClearAll.acquire();
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfClearAll.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object getStatus(final String userId,
      final Continuation<? super StatusEntity> $completion) {
    final String _sql = "SELECT * FROM status_cache WHERE userId = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, userId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<StatusEntity>() {
      @Override
      @Nullable
      public StatusEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "userId");
          final int _cursorIndexOfDoing = CursorUtil.getColumnIndexOrThrow(_cursor, "doing");
          final int _cursorIndexOfCustomDoing = CursorUtil.getColumnIndexOrThrow(_cursor, "customDoing");
          final int _cursorIndexOfFeeling = CursorUtil.getColumnIndexOrThrow(_cursor, "feeling");
          final int _cursorIndexOfFeelingColor = CursorUtil.getColumnIndexOrThrow(_cursor, "feelingColor");
          final int _cursorIndexOfFeelingEmoji = CursorUtil.getColumnIndexOrThrow(_cursor, "feelingEmoji");
          final int _cursorIndexOfNote = CursorUtil.getColumnIndexOrThrow(_cursor, "note");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final int _cursorIndexOfSource = CursorUtil.getColumnIndexOrThrow(_cursor, "source");
          final StatusEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpUserId;
            _tmpUserId = _cursor.getString(_cursorIndexOfUserId);
            final String _tmpDoing;
            _tmpDoing = _cursor.getString(_cursorIndexOfDoing);
            final String _tmpCustomDoing;
            _tmpCustomDoing = _cursor.getString(_cursorIndexOfCustomDoing);
            final String _tmpFeeling;
            _tmpFeeling = _cursor.getString(_cursorIndexOfFeeling);
            final String _tmpFeelingColor;
            _tmpFeelingColor = _cursor.getString(_cursorIndexOfFeelingColor);
            final String _tmpFeelingEmoji;
            _tmpFeelingEmoji = _cursor.getString(_cursorIndexOfFeelingEmoji);
            final String _tmpNote;
            _tmpNote = _cursor.getString(_cursorIndexOfNote);
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            final String _tmpSource;
            _tmpSource = _cursor.getString(_cursorIndexOfSource);
            _result = new StatusEntity(_tmpUserId,_tmpDoing,_tmpCustomDoing,_tmpFeeling,_tmpFeelingColor,_tmpFeelingEmoji,_tmpNote,_tmpUpdatedAt,_tmpSource);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
