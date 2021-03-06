/*
Copyright (c) 2010, Jesper André Lyngesen Pedersen
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are
met:

 - Redistributions of source code must retain the above copyright
   notice, this list of conditions and the following disclaimer.

 - Redistributions in binary form must reproduce the above copyright
   notice, this list of conditions and the following disclaimer in the
   documentation and/or other materials provided with the distribution.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
package org.signaut.common.http;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

public interface SimpleHttpClient {
    public interface HttpResponseHandler<T> {
        T handleInput(int responseCode, InputStream input, HttpURLConnection connection);
    }

    <T> T post(String url, HttpResponseHandler<T> handler, String content, Map<String, String> headers);

    <T> T put(String url, HttpResponseHandler<T> handler, String content, Map<String, String> headers);

    <T> T get(String url, HttpResponseHandler<T> handler, Map<String, String> headers);

    <T> T delete(String url, HttpResponseHandler<T> handler, Map<String, String> headers);

    <T> T post(URL url, HttpResponseHandler<T> handler, String content, Map<String, String> headers);
    
    <T> T put(URL url, HttpResponseHandler<T> handler, String content, Map<String, String> headers);

    <T> T get(URL url, HttpResponseHandler<T> handler, Map<String, String> headers);

    <T> T delete(URL url, HttpResponseHandler<T> handler, Map<String, String> headers);

}
