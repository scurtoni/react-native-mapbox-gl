
package com.mapbox.reactnativemapboxgl;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.util.Log;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.facebook.react.uimanager.SimpleViewManager;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.annotations.ReactProp;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.annotations.PolygonOptions;
import com.mapbox.mapboxsdk.annotations.PolylineOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.TrackingSettings;
import com.mapbox.mapboxsdk.maps.UiSettings;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.annotation.Nullable;

public class ReactNativeMapboxGLManager extends SimpleViewManager<MapView> {

    public static final String REACT_CLASS = "RCTMapbox";

    public static final String PROP_ACCESS_TOKEN = "accessToken";
    public static final String PROP_ANNOTATIONS = "annotations";
    public static final String PROP_CENTER_COORDINATE = "centerCoordinate";
    public static final String PROP_DEBUG_ACTIVE = "debugActive";
    public static final String PROP_DIRECTION = "direction";
    public static final String PROP_ONOPENANNOTATION = "onOpenAnnotation";
    public static final String PROP_ONLONGPRESS = "onLongPress";
    public static final String PROP_ONREGIONCHANGE = "onRegionChange";
    public static final String PROP_ONUSER_LOCATION_CHANGE = "onUserLocationChange";
    public static final String PROP_ROTATION_ENABLED = "rotateEnabled";
    public static final String PROP_SCROLL_ENABLED = "scrollEnabled";
    public static final String PROP_USER_LOCATION = "showsUserLocation";
    public static final String PROP_STYLE_URL = "styleURL";
    public static final String PROP_USER_TRACKING_MODE = "userTrackingMode";
    public static final String PROP_ZOOM_ENABLED = "zoomEnabled";
    public static final String PROP_ZOOM_LEVEL = "zoomLevel";
    public static final String PROP_SET_TILT = "tilt";
    public static final String PROP_COMPASS_IS_HIDDEN = "compassIsHidden";
    public static final String PROP_LOGO_IS_HIDDEN = "logoIsHidden";
    public static final String PROP_ATTRIBUTION_BUTTON_IS_HIDDEN = "attributionButtonIsHidden";
    private MapView mapView;
    private MapboxMap mapboxMap;
    private UiSettings uiSettings;
    private TrackingSettings trackingSettings;

    private MapSettings mapSettings;


    @Override
    public String getName() {
        return REACT_CLASS;
    }

    @Override
    public MapView createViewInstance(ThemedReactContext context) {
        mapView = new MapView(context);
        mapView.setAccessToken("pk.foo");
        mapView.onCreate(null);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        //init map setting bean
        mapSettings = new MapSettings();

        return mapView;
    }

    public void onMapReady(final MapView view, Boolean value) {
        view.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull MapboxMap m) {
                uiSettings = mapboxMap.getUiSettings();
                trackingSettings = mapboxMap.getTrackingSettings();
                mapboxMap = m;

            }
        });
    }


    @ReactProp(name = PROP_ACCESS_TOKEN)
    public void setAccessToken(MapView view, @Nullable String value) {

        mapSettings.setAccessToken(value);

        view.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull MapboxMap m) {

                String accessToken = mapSettings.getAccessToken();

                if (accessToken == null || accessToken.isEmpty()) {
                    Log.e(REACT_CLASS, "Error: No access token provided");
                } else {
                    mapboxMap.setAccessToken(accessToken);
                }
            }
        });

    }

    @ReactProp(name = PROP_SET_TILT)
    public void setTilt(MapView view, @Nullable double pitch) {
        mapboxMap.moveCamera(CameraUpdateFactory.newCameraPosition(
                new CameraPosition.Builder()
                        .tilt(pitch)
                        .build()));
    }

    public static Drawable drawableFromUrl(MapView view, String url) throws IOException {
        Bitmap x;

        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.connect();
        InputStream input = connection.getInputStream();

        x = BitmapFactory.decodeStream(input);
        return new BitmapDrawable(view.getResources(), x);
    }

    @ReactProp(name = PROP_ANNOTATIONS)
    public void setAnnotationClear(MapView view, @Nullable ReadableArray value) {
        setAnnotations(view, value, true);
    }


    public void setAnnotations(MapView view, @Nullable ReadableArray value, boolean clearMap) {
        if (value == null || value.size() < 1) {
            Log.e(REACT_CLASS, "Error: No annotations");
        } else {

            mapSettings.setAnnotations(value);

            view.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(@NonNull MapboxMap m) {

                    ReadableArray annotations = mapSettings.getAnnotations();


                    //   if (clearMap) {
                    mapboxMap.removeAnnotations();
                    //   }
                    int size = annotations.size();
                    for (int i = 0; i < size; i++) {
                        ReadableMap annotation = annotations.getMap(i);
                        String type = annotation.getString("type");
                        if (type.equals("point")) {
                            double latitude = annotation.getArray("coordinates").getDouble(0);
                            double longitude = annotation.getArray("coordinates").getDouble(1);
                            LatLng markerCenter = new LatLng(latitude, longitude);
                            MarkerOptions marker = new MarkerOptions();
                            marker.position(markerCenter);
                            if (annotation.hasKey("title")) {
                                String title = annotation.getString("title");
                                marker.title(title);
                            }
                            if (annotation.hasKey("subtitle")) {
                                String subtitle = annotation.getString("subtitle");
                                marker.snippet(subtitle);
                            }
                            if (annotation.hasKey("annotationImage")) {
                                ReadableMap annotationImage = annotation.getMap("annotationImage");
                                String annotationURL = annotationImage.getString("url");
                                try {
                                    //                            Drawable image = drawableFromUrl(mapView, annotationURL);
                                    //                            IconFactory iconFactory = mapboxMap.getIconFactory();
                                    //                            Icon icon = iconFactory.fromDrawable(image);
                                    //                            marker.icon(icon);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            mapboxMap.addMarker(marker);
                        } else if (type.equals("polyline")) {
                            int coordSize = annotation.getArray("coordinates").size();
                            PolylineOptions polyline = new PolylineOptions();
                            for (int p = 0; p < coordSize; p++) {
                                double latitude = annotation.getArray("coordinates").getArray(p).getDouble(0);
                                double longitude = annotation.getArray("coordinates").getArray(p).getDouble(1);
                                polyline.add(new LatLng(latitude, longitude));
                            }
                            if (annotation.hasKey("alpha")) {
                                double strokeAlpha = annotation.getDouble("alpha");
                                polyline.alpha((float) strokeAlpha);
                            }
                            if (annotation.hasKey("strokeColor")) {
                                int strokeColor = Color.parseColor(annotation.getString("strokeColor"));
                                polyline.color(strokeColor);
                            }
                            if (annotation.hasKey("strokeWidth")) {
                                float strokeWidth = annotation.getInt("strokeWidth");
                                polyline.width(strokeWidth);
                            }
                            mapboxMap.addPolyline(polyline);
                        } else if (type.equals("polygon")) {
                            int coordSize = annotation.getArray("coordinates").size();
                            PolygonOptions polygon = new PolygonOptions();
                            for (int p = 0; p < coordSize; p++) {
                                double latitude = annotation.getArray("coordinates").getArray(p).getDouble(0);
                                double longitude = annotation.getArray("coordinates").getArray(p).getDouble(1);
                                polygon.add(new LatLng(latitude, longitude));
                            }
                            if (annotation.hasKey("alpha")) {
                                double fillAlpha = annotation.getDouble("alpha");
                                polygon.alpha((float) fillAlpha);
                            }
                            if (annotation.hasKey("fillColor")) {
                                int fillColor = Color.parseColor(annotation.getString("fillColor"));
                                polygon.fillColor(fillColor);
                            }
                            if (annotation.hasKey("strokeColor")) {
                                int strokeColor = Color.parseColor(annotation.getString("strokeColor"));
                                polygon.strokeColor(strokeColor);
                            }
                            mapboxMap.addPolygon(polygon);
                        }
                    }

                }
            });


        }
    }

    @ReactProp(name = PROP_DEBUG_ACTIVE, defaultBoolean = false)
    public void setDebugActive(MapView view, Boolean value) {

        mapSettings.setDebugActive(value);

        view.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull MapboxMap m) {

                ReadableArray annotations = mapSettings.getAnnotations();

                mapboxMap.setDebugActive(mapSettings.isDebugActive());
            }}
        );

    }

    @ReactProp(name = PROP_DIRECTION, defaultDouble = 0)
    public void setDirection(MapView view, double value) {


        mapSettings.setDirection(value);

        view.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull MapboxMap m) {

                Double direction = mapSettings.getDirection();

                mapboxMap.moveCamera(CameraUpdateFactory.newCameraPosition(
                        new CameraPosition.Builder()
                                .bearing(direction)
                                .build()));
            }}
        );

    }

    @ReactProp(name = PROP_ONREGIONCHANGE, defaultBoolean = true)
    public void onMapChanged(final MapView view, Boolean value) {
        view.addOnMapChangedListener(new MapView.OnMapChangedListener() {
            @Override
            public void onMapChanged(int change) {
                if (change == MapView.REGION_DID_CHANGE || change == MapView.REGION_DID_CHANGE_ANIMATED) {
                    WritableMap event = Arguments.createMap();
                    WritableMap location = Arguments.createMap();
//                    location.putDouble("latitude", center);
//                    location.putDouble("longitude", mapboxMap.getCenterCoordinate().getLongitude());
//                    location.putDouble("zoom", mapboxMap.getZoomLevel());
                    event.putMap("src", location);
                    ReactContext reactContext = (ReactContext) view.getContext();
                    reactContext
                            .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                            .emit("onRegionChange", event);
                }
            }
        });
    }

    @ReactProp(name = PROP_ONUSER_LOCATION_CHANGE, defaultBoolean = true)
    public void onMyLocationChange(final MapView view, Boolean value) {
        mapboxMap.setOnMyLocationChangeListener(new MapboxMap.OnMyLocationChangeListener() {
            @Override
            public void onMyLocationChange(@Nullable Location location) {
                WritableMap event = Arguments.createMap();
                WritableMap locationMap = Arguments.createMap();
                locationMap.putDouble("latitude", location.getLatitude());
                locationMap.putDouble("longitude", location.getLongitude());
                locationMap.putDouble("accuracy", location.getAccuracy());
                locationMap.putDouble("altitude", location.getAltitude());
                locationMap.putDouble("bearing", location.getBearing());
                locationMap.putDouble("speed", location.getSpeed());
                locationMap.putString("provider", location.getProvider());
                event.putMap("src", locationMap);
                ReactContext reactContext = (ReactContext) view.getContext();
                reactContext
                        .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                        .emit("onUserLocationChange", event);
            }
        });
    }

    @ReactProp(name = PROP_ONOPENANNOTATION, defaultBoolean = true)
    public void onMarkerClick(final MapView view, Boolean value) {
        mapboxMap.setOnMarkerClickListener(new MapboxMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(@Nullable Marker marker) {
                WritableMap event = Arguments.createMap();
                WritableMap markerObject = Arguments.createMap();
                markerObject.putString("title", marker.getTitle());
                markerObject.putString("subtitle", marker.getSnippet());
                markerObject.putDouble("latitude", marker.getPosition().getLatitude());
                markerObject.putDouble("longitude", marker.getPosition().getLongitude());
                event.putMap("src", markerObject);
                ReactContext reactContext = (ReactContext) view.getContext();
                reactContext
                        .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                        .emit("onOpenAnnotation", event);
                return false;
            }
        });
    }

    @ReactProp(name = PROP_ONLONGPRESS, defaultBoolean = true)
    public void onMapLongClick(final MapView view, Boolean value) {
        mapboxMap.setOnMapLongClickListener(new MapboxMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(@Nullable LatLng location) {
                WritableMap event = Arguments.createMap();
                WritableMap loc = Arguments.createMap();
                loc.putDouble("latitude", location.getLatitude());
                loc.putDouble("longitude", location.getLatitude());
                event.putMap("src", loc);
                ReactContext reactContext = (ReactContext) view.getContext();
                reactContext
                        .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                        .emit("onLongPress", event);
            }
        });
    }

    @ReactProp(name = PROP_CENTER_COORDINATE)
    public void setCenterCoordinate(MapView view, @Nullable ReadableMap center) {


        if (center != null) {

            mapSettings.setCenter(center);

            view.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(@NonNull MapboxMap m) {

                    ReadableMap center = mapSettings.getCenter();

                    double latitude = center.getDouble("latitude");
                    double longitude = center.getDouble("longitude");
                    CameraPosition cameraPosition = new CameraPosition.Builder()
                            .target(new LatLng(latitude, longitude))
                            .build();
                    mapboxMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                }
            });


        }else{
            Log.w(REACT_CLASS, "No CenterCoordinate provided");
        }
    }

    @ReactProp(name = PROP_ROTATION_ENABLED, defaultBoolean = true)
    public void setRotateEnabled(MapView view, Boolean value) {

        mapSettings.setRotateEnabled(value);

        view.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull MapboxMap m) {

                boolean routeEnabled = mapSettings.isRotateEnabled();

                uiSettings.setRotateGesturesEnabled(routeEnabled);
            }
        });
    }

    @ReactProp(name = PROP_USER_LOCATION, defaultBoolean = true)
    public void setMyLocationEnabled(MapView view, Boolean value) {

        mapSettings.setMyLocationEnabled(value);

        view.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull MapboxMap m) {

                boolean myLocationEnabled = mapSettings.isMyLocationEnabled();

                mapboxMap.setMyLocationEnabled(myLocationEnabled);
            }
        });

    }

    @ReactProp(name = PROP_STYLE_URL)
    public void setStyleUrl(MapView view, @Nullable String value) {
        if (value != null && !value.isEmpty()) {
            view.setStyleUrl(value);
        }else{
            Log.w(REACT_CLASS, "No StyleUrl provided");
        }
    }

    @ReactProp(name = PROP_USER_TRACKING_MODE, defaultInt = 0)
    public void setMyLocationTrackingMode(MapView view, @Nullable int mode) {


        mapSettings.setTrackingMode(mode);

        view.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull MapboxMap m) {

                int mode = mapSettings.getTrackingMode();

                trackingSettings.setMyLocationTrackingMode(mode);
            }
        });

    }

    @ReactProp(name = PROP_ZOOM_ENABLED, defaultBoolean = true)
    public void setZoomEnabled(MapView view, Boolean value) {

        mapSettings.setZoomEnabled(value);

        view.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull MapboxMap m) {

                uiSettings.setZoomControlsEnabled(mapSettings.getZoomEnabled());
            }
        });

    }

    @ReactProp(name = PROP_ZOOM_LEVEL, defaultFloat = 0f)
    public void setZoomLevel(MapView view, float value) {

        mapSettings.setZoomLevel(value);

        view.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull MapboxMap m) {

                mapboxMap.moveCamera(CameraUpdateFactory.newCameraPosition(
                        new CameraPosition.Builder()
                                .zoom(mapSettings.getZoomLevel())
                                .build()));
            }
        });

    }

    @ReactProp(name = PROP_SCROLL_ENABLED, defaultBoolean = true)
    public void setScrollEnabled(MapView view, Boolean value) {

        mapSettings.setScrollEnabled(value);

        view.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull MapboxMap m) {
                uiSettings.setScrollGesturesEnabled(mapSettings.isScrollEnabled());
            }
        });



    }


    @ReactProp(name = PROP_COMPASS_IS_HIDDEN)
    public void setCompassIsHidden(MapView view, Boolean value) {

        mapSettings.setIsCompassHidden(value);

        view.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull MapboxMap m) {

                uiSettings.setCompassEnabled(mapSettings.getIsCompassHidden());

            }
        });


    }


    /*
    @ReactProp(name = PROP_LOGO_IS_HIDDEN)
    public void setLogoIsHidden(MapView view, Boolean value) {
        uiSettings.setLogoEnabled(value);
    }

    @ReactProp(name = PROP_ATTRIBUTION_BUTTON_IS_HIDDEN)
    public void setAttributionButtonIsHidden(MapView view, Boolean value) {
        uiSettings.setLogoEnabled(value);
    }
    */

    public void setCenterCoordinateZoomLevel(MapView view, @Nullable ReadableMap center) {
        if (center != null) {
            double latitude = center.getDouble("latitude");
            double longitude = center.getDouble("longitude");
            float zoom = (float)center.getDouble("zoom");
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(new LatLng(latitude, longitude))
                    .zoom(zoom)
                    .build();
            mapboxMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        }else{
            Log.w(REACT_CLASS, "No CenterCoordinate provided");
        }
    }

    public void setVisibleCoordinateBounds(MapView view, @Nullable ReadableMap info) {
        final LatLng sw = new LatLng(info.getDouble("latSW"), info.getDouble("lngSW"));
        final LatLng ne = new LatLng(info.getDouble("latNE"), info.getDouble("lngNE"));
        LatLngBounds BOUNDS = new LatLngBounds.Builder().include(sw).include(ne).build();
//        CameraPosition cameraPosition = new CameraPosition.Builder()
//                .target(BOUNDS)
//                .build();

    }

    public void removeAllAnnotations(MapView view, @Nullable Boolean placeHolder) {
        mapboxMap.removeAnnotations();
    }

    public WritableMap getDirection(MapView view) {
        WritableMap callbackDict = Arguments.createMap();
        CameraPosition center = mapboxMap.getCameraPosition();
        callbackDict.putDouble("direction", center.bearing);
        return callbackDict;
    }

    public WritableMap getCenterCoordinateZoomLevel(MapView view) {
        WritableMap callbackDict = Arguments.createMap();
        CameraPosition center = mapboxMap.getCameraPosition();
        callbackDict.putDouble("latitude", center.target.getLatitude());
        callbackDict.putDouble("longitude", center.target.getLongitude());
        callbackDict.putDouble("zoomLevel", center.zoom);

        return callbackDict;
    }
    /*
        public WritableMap getBounds(MapView view) {
            WritableMap callbackDict = Arguments.createMap();
            int viewportWidth = view.getWidth();
            int viewportHeight = view.getHeight();
            if (viewportWidth > 0 && viewportHeight > 0) {
                LatLng ne = view.fromScreenLocation(new PointF(viewportWidth, 0));
                LatLng sw = view.fromScreenLocation(new PointF(0, viewportHeight));
                callbackDict.putDouble("latNE", ne.getLatitude());
                callbackDict.putDouble("lngNE", ne.getLongitude());
                callbackDict.putDouble("latSW", sw.getLatitude());
                callbackDict.putDouble("lngSW", sw.getLongitude());
            }
            return callbackDict;
        }
    */
    public MapView getMapView() {
        return mapView;
    }


    private class MapSettings {
        private boolean isCompassHidden;

        private boolean zoomEnabled;

        private String accessToken;

        private ReadableArray annotations;

        private ReadableMap center;

        private boolean debugActive;

        private Double direction;

        private boolean rotateEnabled;

        private boolean scrollEnabled;

        private String style;

        private boolean showsUserLocation;

        private String styleURL;

        private float zoomLevel;

        private boolean myLocationEnabled;

        private int trackingMode;

        public boolean getIsCompassHidden() {
            return isCompassHidden;
        }

        public void setIsCompassHidden(boolean value) {
            this.isCompassHidden = value;
        }

        public boolean getZoomEnabled() {
            return zoomEnabled;
        }

        public void setZoomEnabled(boolean value) {
            this.zoomEnabled = value;
        }

        public ReadableArray getAnnotations() {
            return annotations;
        }

        public void setAnnotations(ReadableArray annotations) {
            this.annotations = annotations;
        }

        public String getAccessToken() {
            return accessToken;
        }

        public void setAccessToken(String accessToken) {
            this.accessToken = accessToken;
        }

        public ReadableMap getCenter() {
            return center;
        }

        public void setCenter(ReadableMap center) {
            this.center = center;
        }

        /**
         * @return the debugActive
         */
        public boolean isDebugActive() {
            return debugActive;
        }

        /**
         * @param debugActive the debugActive to set
         */
        public void setDebugActive(boolean debugActive) {
            this.debugActive = debugActive;
        }

        /**
         * @return the direction
         */
        public Double getDirection() {
            return direction;
        }

        /**
         * @param direction the direction to set
         */
        public void setDirection(Double direction) {
            this.direction = direction;
        }

        /**
         * @return the rotateEnabled
         */
        public boolean isRotateEnabled() {
            return rotateEnabled;
        }

        /**
         * @param rotateEnabled the rotateEnabled to set
         */
        public void setRotateEnabled(boolean rotateEnabled) {
            this.rotateEnabled = rotateEnabled;
        }

        /**
         * @return the scrollEnabled
         */
        public boolean isScrollEnabled() {
            return scrollEnabled;
        }

        /**
         * @param scrollEnabled the scrollEnabled to set
         */
        public void setScrollEnabled(boolean scrollEnabled) {
            this.scrollEnabled = scrollEnabled;
        }

        /**
         * @return the style
         */
        public String getStyle() {
            return style;
        }

        /**
         * @param style the style to set
         */
        public void setStyle(String style) {
            this.style = style;
        }

        /**
         * @return the showsUserLocation
         */
        public boolean isShowsUserLocation() {
            return showsUserLocation;
        }

        /**
         * @param showsUserLocation the showsUserLocation to set
         */
        public void setShowsUserLocation(boolean showsUserLocation) {
            this.showsUserLocation = showsUserLocation;
        }

        /**
         * @return the styleURL
         */
        public String getStyleURL() {
            return styleURL;
        }

        /**
         * @param styleURL the styleURL to set
         */
        public void setStyleURL(String styleURL) {
            this.styleURL = styleURL;
        }

        /**
         * @return the zoomLevel
         */
        public float getZoomLevel() {
            return zoomLevel;
        }

        /**
         * @param zoomLevel the zoomLevel to set
         */
        public void setZoomLevel(float zoomLevel) {
            this.zoomLevel = zoomLevel;
        }

        /**
         * @param isCompassHidden the isCompassHidden to set
         */
        public void setCompassHidden(boolean isCompassHidden) {
            this.isCompassHidden = isCompassHidden;
        }

        public boolean isMyLocationEnabled() {
            return myLocationEnabled;
        }

        public void setMyLocationEnabled(boolean myLocationEnabled) {
            this.myLocationEnabled = myLocationEnabled;
        }


        public int getTrackingMode() {
            return trackingMode;
        }

        public void setTrackingMode(int trackingMode) {
            this.trackingMode = trackingMode;
        }

    }



    }