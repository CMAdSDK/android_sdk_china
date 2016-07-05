package com.cmcm.adsdk.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;

import com.baidu.mobad.feeds.BaiduNative;
import com.baidu.mobad.feeds.NativeErrorCode;
import com.baidu.mobad.feeds.NativeResponse;
import com.baidu.mobad.feeds.RequestParameters;
import com.cmcm.adsdk.Const;
import com.cmcm.adsdk.base.CMBaseNativeAd;
import com.cmcm.baseapi.ads.INativeAd;
import com.cmcm.utils.Commons;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by chenhao on 15/12/1.
 */
public class BaiduNativeAdapter extends NativeloaderAdapter implements BaiduNative.BaiduNativeNetworkListener{

    private Context mContext;
    private Map<String, Object> mExtras;

    @Override
    public void loadNativeAd(@NonNull Context context,
                                @NonNull Map<String, Object> extras) {

        mContext = context;
        mExtras = extras;
        String mPlacementId = (String)mExtras.get(CMBaseNativeAd.KEY_PLACEMENT_ID);
        BaiduNative baiduNative = new BaiduNative(mContext, mPlacementId, this);
        RequestParameters requestParameters = new RequestParameters.Builder().
                downloadAppConfirmPolicy(RequestParameters.DOWNLOAD_APP_CONFIRM_CUSTOM_BY_APP).build();
        requestParameters.setAdsType(RequestParameters.ADS_TYPE_OPENPAGE | RequestParameters.ADS_TYPE_DOWNLOAD);
        baiduNative.makeRequest(requestParameters);
    }

    @Override
    public int getReportRes() {
        return Const.res.baidu;
    }

    @Override
    public String getReportPkgName(String adTypeName) {
        return Const.pkgName.baidu;
    }

    @Override
    public String getAdKeyType() {
        return Const.KEY_BD;
    }

    @Override
    public long getDefaultCacheTime() {
        return Const.cacheTime.baidu;
    }

    @Override
    public void onNativeLoad(List<NativeResponse> list) {
        List<INativeAd> tempList = new ArrayList<INativeAd>();

        if(list != null && !list.isEmpty()) {
            for(NativeResponse response : list){
                if(response != null && response.isAdAvailable(mContext)){
                    tempList.add(new BaiduNativeAd(response));
                }
            }
        }

        if (tempList.isEmpty()) {
            notifyNativeAdFailed("baidu.fake-fill.invalidad");
        } else {
            notifyNativeAdLoaded(tempList);
        }
    }

    @Override
    public void onNativeFail(NativeErrorCode nativeErrorCode) {
        notifyNativeAdFailed(nativeErrorCode.toString());
    }

    public class BaiduNativeAd extends CMBaseNativeAd {

        private View mAdView;
        final private NativeResponse mNativeResponse;
        public BaiduNativeAd(NativeResponse response) {

            mNativeResponse = response;
            setUpData(response);
        }

        void setUpData(@NonNull NativeResponse nativeResponse){
            setTitle(nativeResponse.getTitle());
            setAdCoverImageUrl(nativeResponse.getImageUrl());
            setAdIconUrl(nativeResponse.getIconUrl());
            setAdBody(nativeResponse.getDesc());
            setIsDownloadApp(nativeResponse.isDownloadApp());
            setAdCallToAction(isDownLoadApp() ? "下载" : "查看");
        }

        @Override
        public String getAdTypeName() {
            return Const.KEY_BD;
        }

        @Override
        public boolean registerViewForInteraction(View view) {
            mAdView = view;
            mNativeResponse.recordImpression(view);
            if (mImpressionListener != null)
                mImpressionListener.onLoggingImpression();
            return false;
        }

        @Override
        public void unregisterView() {
            if(null != mAdView) {
                mAdView = null;
            }
        }

        @Override
        public boolean hasExpired() {
            return !mNativeResponse.isAdAvailable(mContext);
        }

        @Override
        public Object getAdObject() {
            return mNativeResponse;
        }

        @Override
        public void handleClick(){
            mNativeResponse.handleClick(mAdView);// 点击响应
        }
    }
}
