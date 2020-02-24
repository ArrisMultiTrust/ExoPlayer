package com.google.android.exoplayer2.demo;

import android.util.Pair;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;


public class MultiTrustHttpDataSource {

  private static final int DEFAULT_NETWORK_TIMEOUT = 10000;

  public final String proxyUrl;
  private final String authToken;

  public MultiTrustHttpDataSource(String url, String token) {
    proxyUrl = url;
    authToken = token;
  }

  /**
   * Posts a MultiTrust License Request to Widevine License Proxy
   *
   * @param payload The request body (from Widevine CDM)
   */
  Pair<Integer, byte[]> postRequest(byte[] payload) throws IOException {

    HttpURLConnection connection= null;
    try {
      connection = (HttpURLConnection)new URL(proxyUrl).openConnection();
      setHttpPostHeaders(connection);
      setMultiTrustHeaders(connection);

      OutputStream out = new BufferedOutputStream(connection.getOutputStream());
      out.write(payload);
      out.close();

      return post(connection);

    }
    finally {
      if (connection != null) {
        connection.disconnect();
      }
    }
  }

  /**
   * Posts a generic request with an empty body to arbitrary url
   *
   * @param url The request end-point
   */
  Pair<Integer, byte[]> postRequest(String url) throws IOException {
    HttpURLConnection connection= null;
    try {
      connection = (HttpURLConnection)new URL(proxyUrl).openConnection();
      setHttpPostHeaders(connection);

      return post(connection);

    }
    finally {
      if (connection != null) {
        connection.disconnect();
      }
    }
  }


  //Private helper functions

  private Pair<Integer, byte[]> post(HttpURLConnection connection) throws IOException {
    Integer returnCode = connection.getResponseCode();
    Pair<Integer,byte[]> result;
    try {
      InputStream in = new BufferedInputStream(connection.getInputStream());

      return Pair.create(returnCode, convertInputStreamToByteArray(in));

    } catch (IOException e) {
      return Pair.create(returnCode, convertInputStreamToByteArray(connection.getErrorStream()));
    }
  }

  private byte[] convertInputStreamToByteArray(InputStream inputStream) throws IOException {
    byte[] bytes = null;
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    byte data[] = new byte[1024];
    int count;
    while ((count = inputStream.read(data)) != -1) {
      bos.write(data, 0, count);
    }
    bos.flush();
    bos.close();
    inputStream.close();
    bytes = bos.toByteArray();
    return bytes;
  }

  private void setHttpPostHeaders(HttpURLConnection connection) throws ProtocolException {
    connection.setConnectTimeout(DEFAULT_NETWORK_TIMEOUT);
    connection.setRequestMethod("POST");
    connection.setDoInput(true);
    connection.setDoOutput(true);
  }

  private void setMultiTrustHeaders(HttpURLConnection connection) {
    connection.setRequestProperty("Authorization", "bearer " + authToken);
  }
}
