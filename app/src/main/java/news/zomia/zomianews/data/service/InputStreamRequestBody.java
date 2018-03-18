package news.zomia.zomianews.data.service;

import android.content.ContentResolver;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.internal.Util;
import okio.BufferedSink;
import okio.Okio;
import okio.Source;

/**
 * Created by Andrey on 16.03.2018.
 */

public class InputStreamRequestBody extends RequestBody {
    private final MediaType contentType;
    private final ContentResolver contentResolver;
    private final Uri uri;
    private final InputStream inputStream;
    public InputStreamRequestBody(MediaType contentType, ContentResolver contentResolver, Uri uri) throws FileNotFoundException {
        if (uri == null) throw new NullPointerException("uri == null");
        this.contentType = contentType;
        this.contentResolver = contentResolver;
        this.uri = uri;

        inputStream = contentResolver.openInputStream(uri);
    }

    @Nullable
    @Override
    public MediaType contentType() {
        return contentType;
    }

    @Override
    public long contentLength() throws IOException {
        return inputStream.available();
    }

    @Override
    public void writeTo(@NonNull BufferedSink sink) throws IOException {
        Source source = null;
        try {
            source = Okio.source(inputStream);
            sink.writeAll(source);
        } finally {
            Util.closeQuietly(source);
        }
    }
}