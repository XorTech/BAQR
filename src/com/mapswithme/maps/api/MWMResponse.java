package com.mapswithme.maps.api;

import android.content.Context;
import android.content.Intent;

public class MWMResponse
{
  private MWMPoint mPoint;
  private double   mZoomLevel;

  /**
   *
   * @return point, for which user requested more information in MapsWithMe application.
   */
  public MWMPoint getPoint()     { return mPoint; }
  public boolean  hasPoint()     { return mPoint != null; }
  public double   getZoomLevel() { return mZoomLevel; }

  @Override
  public String toString()
  {
    return "MWMResponse [SelectedPoint=" + mPoint + "]";
  }

  /**
   * Factory method to extract response data from intent.
   *
   * @param context
   * @param intent
   * @return
   */
  public static MWMResponse extractFromIntent(Context context, Intent intent)
  {
    final MWMResponse response = new MWMResponse();
    // parse point
    final double lat = intent.getDoubleExtra(Const.EXTRA_MWM_RESPONSE_POINT_LAT, INVALID_LL);
    final double lon = intent.getDoubleExtra(Const.EXTRA_MWM_RESPONSE_POINT_LON, INVALID_LL);
    final String name = intent.getStringExtra(Const.EXTRA_MWM_RESPONSE_POINT_NAME);
    final String id = intent.getStringExtra(Const.EXTRA_MWM_RESPONSE_POINT_ID);

    // parse additional info
    response.mZoomLevel = intent.getDoubleExtra(Const.EXTRA_MWM_RESPONSE_ZOOM, 9);

    if (lat != INVALID_LL && lon != INVALID_LL)
      response.mPoint = new MWMPoint(lat, lon, name, id);
    else
      response.mPoint = null;

    return response;
  }

  private final static double INVALID_LL =  Double.MIN_VALUE;

  private MWMResponse() {}
}
