/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.media;

import com.sun.media.jfxmedia.MediaError;
import java.net.UnknownHostException;

public final class MediaException
extends RuntimeException {
    private final Type type;

    static Type errorCodeToType(int n) {
        Type type = n == MediaError.ERROR_LOCATOR_CONNECTION_LOST.code() ? Type.MEDIA_INACCESSIBLE : (n == MediaError.ERROR_GSTREAMER_SOURCEFILE_NONEXISTENT.code() || n == MediaError.ERROR_GSTREAMER_SOURCEFILE_NONREGULAR.code() ? Type.MEDIA_UNAVAILABLE : (n == MediaError.ERROR_MEDIA_AUDIO_FORMAT_UNSUPPORTED.code() || n == MediaError.ERROR_MEDIA_UNKNOWN_PIXEL_FORMAT.code() || n == MediaError.ERROR_MEDIA_VIDEO_FORMAT_UNSUPPORTED.code() || n == MediaError.ERROR_LOCATOR_CONTENT_TYPE_NULL.code() || n == MediaError.ERROR_LOCATOR_UNSUPPORTED_MEDIA_FORMAT.code() || n == MediaError.ERROR_LOCATOR_UNSUPPORTED_TYPE.code() || n == MediaError.ERROR_GSTREAMER_UNSUPPORTED_PROTOCOL.code() || n == MediaError.ERROR_MEDIA_MP3_FORMAT_UNSUPPORTED.code() || n == MediaError.ERROR_MEDIA_AAC_FORMAT_UNSUPPORTED.code() || n == MediaError.ERROR_MEDIA_H264_FORMAT_UNSUPPORTED.code() || n == MediaError.ERROR_MEDIA_HLS_FORMAT_UNSUPPORTED.code() ? Type.MEDIA_UNSUPPORTED : (n == MediaError.ERROR_MEDIA_CORRUPTED.code() ? Type.MEDIA_CORRUPTED : ((n & MediaError.ERROR_BASE_GSTREAMER.code()) == MediaError.ERROR_BASE_GSTREAMER.code() || (n & MediaError.ERROR_BASE_JNI.code()) == MediaError.ERROR_BASE_JNI.code() ? Type.PLAYBACK_ERROR : Type.UNKNOWN))));
        return type;
    }

    static MediaException exceptionToMediaException(Exception exception) {
        com.sun.media.jfxmedia.MediaException mediaException;
        MediaError mediaError;
        Type type = Type.UNKNOWN;
        if (exception.getCause() instanceof UnknownHostException) {
            type = Type.MEDIA_UNAVAILABLE;
        } else if (exception.getCause() instanceof IllegalArgumentException) {
            type = Type.MEDIA_UNSUPPORTED;
        } else if (exception instanceof com.sun.media.jfxmedia.MediaException && (mediaError = (mediaException = (com.sun.media.jfxmedia.MediaException)exception).getMediaError()) != null) {
            type = MediaException.errorCodeToType(mediaError.code());
        }
        return new MediaException(type, (Throwable)exception);
    }

    static MediaException haltException(String string) {
        return new MediaException(Type.PLAYBACK_HALTED, string);
    }

    static MediaException getMediaException(Object object, int n, String string) {
        String string2 = MediaError.getFromCode(n).description();
        String string3 = "[" + object + "] " + string + ": " + string2;
        Type type = MediaException.errorCodeToType(n);
        return new MediaException(type, string3);
    }

    MediaException(Type type, Throwable throwable) {
        super(throwable);
        this.type = type;
    }

    MediaException(Type type, String string, Throwable throwable) {
        super(string, throwable);
        this.type = type;
    }

    MediaException(Type type, String string) {
        super(string);
        this.type = type;
    }

    public Type getType() {
        return this.type;
    }

    @Override
    public String toString() {
        String string = "MediaException: " + this.type;
        if (this.getMessage() != null) {
            string = string + " : " + this.getMessage();
        }
        if (this.getCause() != null) {
            string = string + " : " + this.getCause();
        }
        return string;
    }

    public static enum Type {
        MEDIA_CORRUPTED,
        MEDIA_INACCESSIBLE,
        MEDIA_UNAVAILABLE,
        MEDIA_UNSPECIFIED,
        MEDIA_UNSUPPORTED,
        OPERATION_UNSUPPORTED,
        PLAYBACK_ERROR,
        PLAYBACK_HALTED,
        UNKNOWN;

    }
}

