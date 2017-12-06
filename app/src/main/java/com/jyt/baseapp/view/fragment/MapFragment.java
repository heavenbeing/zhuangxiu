package com.jyt.baseapp.view.fragment;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.GeocodeQuery;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.jyt.baseapp.R;
import com.jyt.baseapp.bean.BrandBean;
import com.jyt.baseapp.bean.MapBean;
import com.jyt.baseapp.bean.SearchBean;
import com.jyt.baseapp.model.MapModel;
import com.jyt.baseapp.util.BaseUtil;
import com.jyt.baseapp.view.activity.ShopActivity;
import com.jyt.baseapp.view.widget.MapSelector;
import com.jyt.baseapp.view.widget.SingleSelector;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;


/**
 * @author LinWei on 2017/10/30 15:04
 */
public class MapFragment extends BaseFragment implements View.OnClickListener, GeocodeSearch.OnGeocodeSearchListener {

    private int mtotalWidth;
    private int mtotalHeight;
    @BindView(R.id.mapview_map)
    MapView mMapView;
    @BindView(R.id.tv_map_city)
    TextView tv_city;
    @BindView(R.id.tv_map_brand)
    TextView tv_brand;
    @BindView(R.id.ll_map_gan)
    LinearLayout line;
    @BindView(R.id.selector_city)
    MapSelector mMapSelector;
    @BindView(R.id.selector_brand)
    SingleSelector mBrandSelector;
    @BindView(R.id.fl_selector)
    FrameLayout fl_selector;
    private MapModel mMapModel;
    private MapBean mMapBean;
    private List<BrandBean> mBrandBeen;
    private List<BrandBean> mBrandSonBeen;
    private int selecrotHeight=BaseUtil.dip2px(380);
    private boolean isHideMapSelecotr; //是否展开pop
    private boolean isHideBrandSelecotr;
    private boolean isShowMap=true;  //是否展开Map
    private boolean isShowBrand=true;    //是否展开Brand
    public AMapLocationClient mLocationClient=null;
    public AMapLocationListener mLocationListener;
    private AMap mMap;
    private boolean isLocation;
    private List<Marker> mMarkerList;
    private GeocodeSearch mGeocodeSearch;
    private String str_province;//搜索的省份
    private String str_brandID;//搜索的品牌ID
    private String str_city;//搜索的城市
    private String str_area;//搜索的地区
    private boolean isByMap;//是否通过省份-城市-地区搜索商店数据，true：定位到该地区；false：不移动


    @Override
    protected int getLayoutId() {
        return R.layout.fragment_map;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mMapView.onCreate(savedInstanceState);
        init();
        initMap();
        initSelecotr();
        initData();
        initLocation();
        initListener();
    }

    private void init(){
        mMapBean=new MapBean();
        mBrandBeen=new ArrayList<>();
        mBrandSonBeen=new ArrayList<>();
        mMarkerList = new ArrayList<>();
        mMapModel = new MapModel();
        WindowManager wm= (WindowManager) BaseUtil.getContext().getSystemService(Context.WINDOW_SERVICE);
        mtotalWidth=wm.getDefaultDisplay().getWidth();
        mtotalHeight=wm.getDefaultDisplay().getHeight();
    }

    private void initMap(){
        mMap = mMapView.getMap();
        mGeocodeSearch = new GeocodeSearch(getActivity());
        mGeocodeSearch.setOnGeocodeSearchListener(this);
        mMap.getUiSettings().setZoomControlsEnabled(false);//隐藏缩放按钮
        mMap.getUiSettings().setRotateGesturesEnabled(false);//旋转
        mMap.getUiSettings().setTiltGesturesEnabled(false);//倾斜
        mMap.setOnCameraChangeListener(new AMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {

            }

            @Override
            public void onCameraChangeFinish(CameraPosition cameraPosition) {
                LatLng l1=mMap.getProjection().fromScreenLocation(new Point(0,mtotalHeight));
                LatLng l2=mMap.getProjection().fromScreenLocation(new Point(mtotalWidth,0));
//                Log.e("@#","longitude1="+l1.longitude+" latitude1="+l1.latitude);
//                Log.e("@#","longitude2="+l2.longitude+" latitude2="+l2.latitude);
//                if (!isFst){
//                    getLocationShop(l1,l2);
//                    isFst=true;
//                }
                getLocationShop(l1,l2);
            }
        });

        mMap.setOnMarkerClickListener(new AMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                SearchBean localData= (SearchBean) marker.getObject();
                Log.e("@#","local:"+localData.getProjectName());
                Intent intent = new Intent(getActivity(),ShopActivity.class);
                intent.putExtra("shopinfo", (Serializable) localData);
                startActivity(intent);
                return true;
            }
        });
    }


    private void initSelecotr(){
        mMapSelector.getLayoutParams().width= (int) (mtotalWidth*0.9);
        mMapSelector.getLayoutParams().height= 0;
        mMapSelector.requestLayout();
        mMapSelector.setHideDeleteIV(true);
        mBrandSelector.getLayoutParams().width= (int) (mtotalWidth*0.9);
        mBrandSelector.getLayoutParams().height= 0;
        mBrandSelector.requestLayout();
        mBrandSelector.setHideDeleteIV(true);
    }


    /**
     * 定位
     */
    private void initLocation(){
        mLocationClient=new AMapLocationClient(getContext().getApplicationContext());
        mLocationListener=new AMapLocationListener() {
            @Override
            public void onLocationChanged(AMapLocation aMapLocation) {
                if (aMapLocation!=null){

                    if (aMapLocation.getErrorCode()==0){
                        if (!isLocation){
                            aMapLocation.getLocationType();
                            double v=aMapLocation.getLatitude();//获取纬度
                            double v1=aMapLocation.getLongitude();//获取经度
                            LatLng latLng2=new LatLng(v,v1);
//                            mMap.addMarker(new MarkerOptions()
//                                    .position(latLng2)
//                                    .icon(BitmapDescriptorFactory
//                                            .defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
                            mMap.moveCamera(CameraUpdateFactory.changeLatLng(latLng2));
                            isLocation=true;
                        }

                    }
                }
            }
        };

        AMapLocationClientOption option=new AMapLocationClientOption();
        option.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        option.setOnceLocation(false);
        option.setNeedAddress(true);
        option.setOnceLocation(false);
        option.setHttpTimeOut(2000);
        option.setWifiActiveScan(true);
        option.setMockEnable(false);
        //设置定位间隔,单位毫秒,默认为2000ms
        option.setInterval(2000);
        mLocationClient.setLocationListener(mLocationListener);
        mLocationClient.setLocationOption(option);
        mLocationClient.startLocation();
    }

    private void initListener(){
        tv_city.setOnClickListener(this);
        tv_brand.setOnClickListener(this);
    }

    private void initData(){
        mMapModel.getProvinceData(getActivity(),new MapModel.onResultProvinceListener() {
            @Override
            public void ResultData(boolean isSuccess, Exception e, List<MapBean.Province> data) {
                if (isSuccess){
                    mMapBean.mProvinces=data;
                    str_province=mMapBean.mProvinces.get(0).ProvinceName;//默认第一个省份
                    mMapBean.mProvinces.get(0).isCheckProvince=true;
                    mMapSelector.setProvinceAdapter(mMapBean,getActivity());
                }
            }
        });

        mMapModel.getCityAreaData(3, new MapModel.onResultCityListener() {
            @Override
            public void ResultData(boolean isSuccess, Exception e, List<MapBean.City> data) {
                if (isSuccess){
                    mMapBean.mCities=data;

                    mMapSelector.setCityAdapter(mMapBean,getActivity());
                }
            }
        });


        mMapSelector.setOnMapClickListener(new MapSelector.OnMapClickListener() {
            @Override
            public void onClickProvince(int ProvinceID, String ProvinceName) {
                ChangeProvince(ProvinceID);
                str_province=ProvinceName;


            }

            @Override
            public void onClickArea(int CityID, String CityName, int AreaID, String AreaName) {
                isByMap=true;
                str_city=CityName;
                str_area=AreaName;
                if ("北京".equals(str_province)
                        || "上海".equals(str_province)
                        || "天津".equals(str_province)
                        || "重庆".equals(str_province)){
                    str_city=str_province+"市";
                    str_area=CityName+AreaName;
                }else if ("台湾".equals(str_province)){
                    str_city=AreaName;
                    str_area=CityName+AreaName;
                }
                SearchShop("null,"+str_province+","+CityName+","+AreaName+",null,null,null");
                tv_city.performClick();
            }

            @Override
            public void onClickBack() {

            }
        });

        mMapModel.getBrandData(new MapModel.OngetBrandResultListener() {
            @Override
            public void Result(boolean isSuccess, List<BrandBean> brandData) {
                if (isSuccess){
                    mBrandBeen=brandData;
                    str_brandID =mBrandBeen.get(0).getBrandId();//默认第一个品牌名
                    mBrandSelector.setLeftAdapter(getActivity(),mBrandBeen);
                }
            }
        });

        //6d78da2a-bd23-11e7-bb43-00ffaa44f255
        mMapModel.getBrandSonData("9ef3c864-b53d-11e7-9b64-00ffaa44255a", new MapModel.OngetBrandResultListener() {
            @Override
            public void Result(boolean isSuccess, List<BrandBean> brandData) {
                if (isSuccess){
                    mBrandSonBeen=brandData;
                    mBrandSelector.setRightAdapter(getActivity(),mBrandSonBeen);
                }
            }
        });

        mBrandSelector.setOnSingleClickListener(new SingleSelector.OnSingleClickListener() {

            @Override
            public void onClickBrand(String BrandID, String BrandName) {
                ChangeBrand(BrandID);
                str_brandID =BrandID;
            }

            @Override
            public void onClickDetail(String BrandSonID, String BrandSonName) {
                isByMap=false;
                tv_brand.performClick();
                SearchShop("null,null,null,null,"+ str_brandID +","+BrandSonID+",null");
            }

            @Override
            public void onClickBack() {

            }
        });




    }



    /**
     * 点击省，改变市级列表
     * @param ProcinveID
     */
    private void ChangeProvince(int ProcinveID){
        mMapModel.getCityAreaData(ProcinveID, new MapModel.onResultCityListener() {
            @Override
            public void ResultData(boolean isSuccess, Exception e, List<MapBean.City> data) {
                if (isSuccess){
                    mMapBean.mCities=data;
                    mMapSelector.notifyData(mMapBean);
                }
            }
        });
    }

    /**
     * 改变品牌
     * @param BrandID
     */
    private void ChangeBrand(String BrandID){
        mMapModel.getBrandSonData(BrandID, new MapModel.OngetBrandResultListener() {
            @Override
            public void Result(boolean isSuccess, List<BrandBean> brandData) {
                if (isSuccess){
                    mBrandSonBeen=brandData;
                    mBrandSelector.notifyRightData(mBrandSonBeen);
                }
            }
        });
    }

    /**
     * 屏幕滑动，通过左下右上两角找寻当前界面内的商店数据
     * @param l1
     * @param l2
     */
    private void getLocationShop(LatLng l1,LatLng l2){
        if (mMarkerList!=null &&mMarkerList.size()>0){
            //清除原有的数据
            for (Marker marker:mMarkerList){
                marker.destroy();
            }
            mMarkerList.clear();
        }
        //添加后续新的数据
        mMapModel.getLocationShop(l1, l2, new MapModel.OnLocationShopResultListener() {
            @Override
            public void Result(boolean isSuccess, List<SearchBean> shops) {
                if (isSuccess){
                    Log.e("@#","size="+shops.size());
                    for (int i = 0; i < shops.size(); i++) {
                        View view=View.inflate(getActivity(),R.layout.layout_infowindow,null);
                        TextView tv= (TextView) view.findViewById(R.id.tv_text);
                        tv.setText(shops.get(i).getProjectName());
                        Bitmap b=convertViewToBitmap(view);
                        LatLng l=new LatLng(Double.valueOf(shops.get(i).getLatitude()),Double.valueOf(shops.get(i).getLongitude()));
                        Marker marker=mMap.addMarker(new MarkerOptions()
                        .position(l)
                        .infoWindowEnable(false)
                        .icon(BitmapDescriptorFactory.fromBitmap(b)));
                        marker.setObject(shops.get(i));
                        mMarkerList.add(marker);
                    }
                }

            }
        });
    }



    private void SearchShop(String condition){
        if (isByMap){
            //地理编码 定位到该位置
            GeocodeQuery query=new GeocodeQuery(str_area,str_city);
            mGeocodeSearch.getFromLocationNameAsyn(query);
        }
        //搜索该定位附近的店
        mMapModel.getSearchData(condition, new MapModel.OnSearchResultListener() {
            @Override
            public void Result(boolean isSuccess, List<SearchBean> data) {
                if (isSuccess){
                    setSearchShop(data);
                }
            }
        });
    }

    private void setSearchShop(List<SearchBean> data){
        if (mMarkerList!=null &&mMarkerList.size()>0){
            //清除原有的数据
            for (Marker marker:mMarkerList){
                marker.destroy();
            }
            mMarkerList.clear();
        }
        for (int i=0;i<data.size();i++){
            SearchBean shop =data.get(i);
            View view=View.inflate(getActivity(),R.layout.layout_infowindow,null);
            TextView tv= (TextView) view.findViewById(R.id.tv_text);
            tv.setText(shop.getProjectName());
            Bitmap b=convertViewToBitmap(view);
            LatLng l=new LatLng(Double.valueOf(shop.getLatitude()),Double.valueOf(shop.getLongitude()));
            Marker marker=mMap.addMarker(new MarkerOptions()
                    .position(l)
                    .anchor(0,0)
                    .infoWindowEnable(false)
                    .icon(BitmapDescriptorFactory.fromBitmap(b)));
            marker.setObject(shop);
            mMarkerList.add(marker);
        }

    }


    /**
     * 生产PopupWindow
     * @param popView
     * @return
     */
    private PopupWindow CreatePopWindow(View popView) {
        final PopupWindow popupWindow = new PopupWindow(popView, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, true);
        popupWindow.setTouchable(true);
        popupWindow.setFocusable(false);
        popupWindow.setOutsideTouchable(false);
        popupWindow.setBackgroundDrawable(new BitmapDrawable(getResources(), (Bitmap) null));
        popupWindow.getContentView().setFocusableInTouchMode(false);
        popupWindow.getContentView().setFocusable(true);
        popupWindow.getContentView().setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_MENU && event.getRepeatCount() == 0
                        && event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (popupWindow != null && popupWindow.isShowing()) {
                        popupWindow.dismiss();
                    }
                    return true;
                }
                return false;
            }
        });
        return popupWindow;
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.tv_map_city:
                if (isShowMap){
                    tv_city.setText("城市∧");
                    tv_city.setTextColor(getResources().getColor(R.color.map_text2));
                    tv_brand.setText("品牌∨");
                    tv_brand.setTextColor(getResources().getColor(R.color.text_color1));
                    mMapSelector.setVisibility(View.VISIBLE);
                    mBrandSelector.setVisibility(View.GONE);
                    isShowMap=false;
                    isShowBrand=true;
                    isHideMapSelecotr=false;
                }else {
                    tv_city.setText("城市∨");
                    tv_city.setTextColor(getResources().getColor(R.color.text_color1));
                    isShowMap=true;
                }
                setMapSelector();
                break;
            case R.id.tv_map_brand:
                if (isShowBrand){
                    tv_brand.setText("品牌∧");
                    tv_brand.setTextColor(getResources().getColor(R.color.map_text2));
                    tv_city.setText("城市∨");
                    tv_city.setTextColor(getResources().getColor(R.color.text_color1));
                    mMapSelector.setVisibility(View.GONE);
                    mBrandSelector.setVisibility(View.VISIBLE);
                    isShowBrand=false;
                    isShowMap=true;
                    isHideBrandSelecotr=false;
                }else {
                    tv_brand.setText("品牌∨");
                    tv_brand.setTextColor(getResources().getColor(R.color.text_color1));
                    isShowBrand=true;

                }
                setBrandSelector();
                break;
        }
    }

    /**
     * 地图的选择器动画
     */
    private void setMapSelector(){
        ValueAnimator animator=null;
        if (!isHideMapSelecotr){
            animator=ValueAnimator.ofInt(0,selecrotHeight);
            isHideMapSelecotr =true;
        }else {
            animator=ValueAnimator.ofInt(selecrotHeight,0);
            isHideMapSelecotr =false;
        }
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mMapSelector.getLayoutParams().height= (int) animation.getAnimatedValue();
                mMapSelector.requestLayout();
            }
        });
        animator.setDuration(500);
        animator.start();
    }

    /**
     * 品牌的选择器动画
     */
    private void setBrandSelector(){
        ValueAnimator animator=null;
        if (!isHideBrandSelecotr){
            animator=ValueAnimator.ofInt(0,selecrotHeight);
            isHideBrandSelecotr =true;
        }else {
            animator=ValueAnimator.ofInt(selecrotHeight,0);
            isHideBrandSelecotr =false;
        }
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mBrandSelector.getLayoutParams().height= (int) animation.getAnimatedValue();
                mBrandSelector.requestLayout();
            }
        });
        animator.setDuration(500);
        animator.start();
    }



    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }


    /**
     * 将view转为位图
     * @param view
     * @return
     */
    public static Bitmap convertViewToBitmap(View view) {
        view.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        view.buildDrawingCache();
        Bitmap bitmap = view.getDrawingCache();
        return bitmap;
    }


    @Override
    public void onRegeocodeSearched(RegeocodeResult regeocodeResult, int i) {

    }

    @Override
    public void onGeocodeSearched(GeocodeResult geocodeResult, int i) {
        if ("钓鱼岛".equals(str_province)){
            mMap.moveCamera(CameraUpdateFactory.changeLatLng(new LatLng(25.744676,123.476492)));
            return;
        }
        LatLonPoint point=geocodeResult.getGeocodeAddressList().get(0).getLatLonPoint();
        LatLng latLng = new LatLng(point.getLatitude(),point.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.changeLatLng(latLng));
    }
}
