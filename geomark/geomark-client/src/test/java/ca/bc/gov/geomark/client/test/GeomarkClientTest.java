package ca.bc.gov.geomark.client.test;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import ca.bc.gov.geomark.client.api.GeomarkClient;

public class GeomarkClientTest {
  // Production
  private static String baseUrl = "https://apps.gov.bc.ca/pub/geomark";

  private static String geomarkGroupId = "gg-C675C9D3AAEB401698D9822701925A7C";

  private static String geomarkGroupSecretKey = "kg-53985F2055364DF59430E3593A6B5D3C";

  private static String geomarkId1;

  private static String geomarkId2;

  // Change one of the if(false) to if (true) to use that config
  static {
    // dlv-geomark.apps.gov.bc.ca
    if (true) {
      baseUrl = "https://dlv-geomark.apps.gov.bc.ca/pub/geomark";
      geomarkGroupId = "gg-9C7DA6EE58706F15E04400144FA876C2";
      geomarkGroupSecretKey = "kg-8A8EA447332A4E4B8D3B227B4E5555B0";

    }
  }

  static {
    // geomark.localhost
    if (false) {
      baseUrl = "https://geomark.localhost/pub/geomark";
      geomarkGroupId = "gg-DCA60701BC2D49AB803BAC9B7D39BAB7";
      geomarkGroupSecretKey = "kg-BBE5CF3814E24AD48EE76ECD525FC865";

      // Allow self signed certs in dev
      final TrustManager[] trustAllCerts = new TrustManager[] {
        new X509TrustManager() {
          @Override
          public void checkClientTrusted(final X509Certificate[] certs, final String authType) {
          }

          @Override
          public void checkServerTrusted(final X509Certificate[] certs, final String authType) {
          }

          @Override
          public java.security.cert.X509Certificate[] getAcceptedIssuers() {
            return null;
          }
        }
      };

      try {
        // Install the all-trusting trust manager
        final SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, trustAllCerts, new java.security.SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

        // Create all-trusting host name verifier
        final HostnameVerifier allHostsValid = new HostnameVerifier() {
          @Override
          public boolean verify(final String hostname, final SSLSession session) {
            return true;
          }
        };

        // Install the all-trusting host verifier
        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
      } catch (final KeyManagementException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } catch (final NoSuchAlgorithmException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
  }

  public static void main(final String[] args) throws IOException {
    testCreateGeomark();
    testCopyGeomark();
    testGetGeomarkInfo();
    testGetGeomarkFeature();
    testGetGeomarkParts();
    testGetGeomarkPoint();
    testGetGeomarkBoundingBox();
    testAddGeomarkToGroup();
    testDeleteGeomarkFromGroup();
    testDeleteGeomarkFromGroupWithoutExpiry();
  }

  public static void testAddGeomarkToGroup() {
    System.out.println("Add Geomark To Group");

    final GeomarkClient client = new GeomarkClient(baseUrl);

    Map<String, Object> response = client.addGeomarksToGroup(geomarkGroupId, geomarkGroupSecretKey,
      "gm-abcdefghijklmnopqrstuvwxyz0000bc");
    System.out.println(response);

    response = client.addGeomarksToGroup(geomarkGroupId, geomarkGroupSecretKey,
      "gm-abcdefghijklmnopqrstuv0bcislands");
    System.out.println(response);

    final List<String> geomarkIds = Arrays.asList("gm-abcdefghijklmnopqrstuv0bcislands",
      geomarkId1);
    response = client.addGeomarksToGroup(geomarkGroupId, geomarkGroupSecretKey, geomarkIds);
    System.out.println(response);

  }

  public static void testCopyGeomark() {
    System.out.println("Copy Geomark");
    final GeomarkClient client = new GeomarkClient(baseUrl);
    final Map<String, Object> parameters = new HashMap<>();
    parameters.put("bufferMetres", 2);

    final List<String> geomarks = Arrays.asList(geomarkId1);

    String geomarkUrl = client.copyGeomark(parameters, geomarks);
    System.out.println(geomarkUrl);

    geomarkUrl = client.copyGeomark(parameters, geomarkId2);
    System.out.println(geomarkUrl);
  }

  public static void testCreateGeomark() {
    System.out.println("Create Geomark");

    final GeomarkClient client = new GeomarkClient(baseUrl);

    final String geometry = "POLYGON((-112 47,-144 47,-144 60,-112 60,-112 47))";

    final Map<String, Object> parameters = new HashMap<>();
    parameters.put(GeomarkClient.SRID, 4326);
    parameters.put(GeomarkClient.FORMAT, "wkt");

    final String geomarkUrl1 = client.addGeomark(geometry, parameters);
    System.out.println(geomarkUrl1);
    geomarkId1 = geomarkUrl1.substring(geomarkUrl1.lastIndexOf('/') + 1);

    final InputStream geomarkIn = client
      .getGeomarkFeatureStream("gm-abcdefghijklmnopqrstuv0bcislands", "wkt", 4326);
    final String geomarkUrl2 = client.addGeomark(geomarkIn, parameters);
    System.out.println(geomarkUrl2);

    geomarkId2 = geomarkUrl2.substring(geomarkUrl2.lastIndexOf('/') + 1);
  }

  public static void testDeleteGeomarkFromGroup() {
    System.out.println("Delete Geomark From Group");

    final GeomarkClient client = new GeomarkClient(baseUrl);

    Map<String, Object> response = client.deleteGeomarksFromGroup(geomarkGroupId,
      geomarkGroupSecretKey, 90, "gm-abcdefghijklmnopqrstuv0bcislands");
    System.out.println(response);

    final List<String> geomarkIds = Arrays.asList("gm-abcdefghijklmnopqrstuv0bcislands",
      geomarkId1);
    response = client.deleteGeomarksFromGroup(geomarkGroupId, geomarkGroupSecretKey, 95,
      geomarkIds);
    System.out.println(response);
  }

  public static void testDeleteGeomarkFromGroupWithoutExpiry() {
    System.out.println("Delete Geomark From Group");

    final GeomarkClient client = new GeomarkClient(baseUrl);

    Map<String, Object> response = client.deleteGeomarksFromGroup(geomarkGroupId,
      geomarkGroupSecretKey, 0, "gm-abcdefghijklmnopqrstuv0bcislands");
    System.out.println(response);

    final List<String> geomarkIds = Arrays.asList("gm-abcdefghijklmnopqrstuv0bcislands");
    response = client.deleteGeomarksFromGroup(geomarkGroupId, geomarkGroupSecretKey, 0, geomarkIds);
    System.out.println(response);

  }

  public static void testGetGeomarkBoundingBox() throws IOException {
    System.out.println("Get Geomark Bounding Box");
    final GeomarkClient client = new GeomarkClient(baseUrl);

    final String geomarkId = "gm-abcdefghijklmnopqrstuv0bcislands";

    final String geomarkUrl = client.getGeomarkBoundingBoxUrl(geomarkId, "wkt", 4326);
    System.out.println(geomarkUrl);

    final InputStream in = client.getGeomarkBoundingBoxStream(geomarkId, "kml", 4326);
    try {

    } finally {
      in.close();
    }

  }

  public static void testGetGeomarkFeature() throws IOException {
    System.out.println("Get Geomark Feature");
    final GeomarkClient client = new GeomarkClient(baseUrl);

    final String geomarkId = "gm-abcdefghijklmnopqrstuv0bcislands";

    final String geomarkUrl = client.getGeomarkFeatureUrl(geomarkId, "wkt", 4326);
    System.out.println(geomarkUrl);

    final InputStream in = client.getGeomarkFeatureStream(geomarkId, "kml", 4326);
    try {

    } finally {
      in.close();
    }

  }

  public static void testGetGeomarkInfo() throws IOException {
    System.out.println("Get Geomark Info");

    final GeomarkClient client = new GeomarkClient(baseUrl);

    final String geomarkId = "gm-abcdefghijklmnopqrstuv0bcislands";

    final Map<String, Object> geomark = client.getGeomarkInfo(geomarkId);
    System.out.println(geomark);

    final String geomarkUrl = client.getGeomarkInfoUrl(geomarkId, "json");
    System.out.println(geomarkUrl);

    final InputStream in = client.getGeomarkInfoStream(geomarkId, "json");
    try {

    } finally {
      in.close();
    }

  }

  public static void testGetGeomarkParts() throws IOException {
    System.out.println("Get Geomark Parts");

    final GeomarkClient client = new GeomarkClient(baseUrl);

    final String geomarkId = "gm-abcdefghijklmnopqrstuv0bcislands";

    final String geomarkUrl = client.getGeomarkPartsUrl(geomarkId, "wkt", 4326);
    System.out.println(geomarkUrl);

    final InputStream in = client.getGeomarkPartsStream(geomarkId, "kml", 4326);
    try {

    } finally {
      in.close();
    }
  }

  public static void testGetGeomarkPoint() throws IOException {
    System.out.println("Get Geomark Point");

    final GeomarkClient client = new GeomarkClient(baseUrl);

    final String geomarkId = "gm-abcdefghijklmnopqrstuv0bcislands";

    final String geomarkUrl = client.getGeomarkPointUrl(geomarkId, "wkt", 4326);
    System.out.println(geomarkUrl);

    final InputStream in = client.getGeomarkPointStream(geomarkId, "kml", 4326);
    try {

    } finally {
      in.close();
    }

  }
}
