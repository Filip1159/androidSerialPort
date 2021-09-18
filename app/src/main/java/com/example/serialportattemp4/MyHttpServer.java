package com.example.serialportattemp4;

import java.io.IOException;

import fi.iki.elonen.NanoHTTPD;

public class MyHttpServer extends NanoHTTPD {
    public static final int PORT = 8765;

    public MyHttpServer() throws IOException {
        super(PORT);
    }

    @Override
    public Response serve(IHTTPSession session) {
        String uri = session.getUri();

        if (uri.equals("/hello")) {
            String response = "HelloWorld";
            return newFixedLengthResponse(response);
        }
        return  null;
    }
}
