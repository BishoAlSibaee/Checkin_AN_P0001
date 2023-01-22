package com.syriasoft.hotelservices.lock;

import android.app.Activity;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.syriasoft.hotelservices.ToastMaker;
import com.ttlock.bl.sdk.api.ExtendedBluetoothDevice;
import com.ttlock.bl.sdk.api.TTLockClient;
import com.ttlock.bl.sdk.callback.InitLockCallback;
import com.ttlock.bl.sdk.constant.FeatureValue;
import com.ttlock.bl.sdk.entity.LockError;
import com.ttlock.bl.sdk.util.FeatureValueUtil;

import java.util.LinkedList;



/**
 * Created on  2019/4/12 0012 14:19
 *
 * @author theodre
 */
public class LockListAdapter extends  RecyclerView.Adapter<LockListAdapter.DeviceViewHolder>{

    private LinkedList<ExtendedBluetoothDevice> mDataList = new LinkedList<ExtendedBluetoothDevice>();

    private Activity mContext;
    private static final int TIMEOUT = 5000;
    private LinkedList<ExtendedBluetoothDevice> mAddStatusList = new LinkedList<>();
    private LinkedList<ExtendedBluetoothDevice> mNormalStatusList = new LinkedList<>();
    private long lastSyncTimeStamp = 0;
    private onLockItemClick mListener;
    public interface onLockItemClick {
        void onClick(ExtendedBluetoothDevice device);
    }

    public void setOnLockItemClick(onLockItemClick click){
        this.mListener = click;
    }

    public LockListAdapter(Activity context , LinkedList<ExtendedBluetoothDevice> mDataList){
        this.mContext = context;
        this.mDataList = mDataList ;
    }

    public synchronized void updateData(ExtendedBluetoothDevice device){
        if(device != null) {
            if(device.isSettingMode()){
                addOrSortLock(device,mAddStatusList);
                removeOtherStatusLock(device,mNormalStatusList);
            }else {
                addOrSortLock(device,mNormalStatusList);
                removeOtherStatusLock(device,mAddStatusList);
            }

            long currentTime = System.currentTimeMillis();
            if((currentTime - lastSyncTimeStamp) >= 800 ){
                if(!mDataList.isEmpty()){
                    mDataList.clear();
                }

                mDataList.addAll(0,mAddStatusList);
                mDataList.addAll(mNormalStatusList);
                notifyDataSetChanged();
                lastSyncTimeStamp = currentTime;
            }
        }
    }


    /**
     * you can sort the lock that be discovered by signal value.
     */
    private void addOrSortLock(ExtendedBluetoothDevice scanDevice, LinkedList<ExtendedBluetoothDevice> lockList){
        boolean isContained = false;
        int length = lockList.size();
        ExtendedBluetoothDevice mTopOneDevice;
        scanDevice.setDate(System.currentTimeMillis());
        if(length > 0){

            mTopOneDevice = lockList.get(0);

            for(int i = 0;i < length;i++) {

                ExtendedBluetoothDevice currentDevice = lockList.get(i);

                if(scanDevice.getAddress().equals(currentDevice.getAddress()) ){
                    isContained = true;
                    if(i != 0 && scanDevice.getRssi() > mTopOneDevice.getRssi()){
                        lockList.remove(i);
                        lockList.add(0,scanDevice);
                    }else {
                        currentDevice.setDate(System.currentTimeMillis());
                        lockList.set(i,currentDevice);
                    }
                }else {
                    if(System.currentTimeMillis() - currentDevice.getDate() >= TIMEOUT) {
                        lockList.remove(i);
                        length = lockList.size();
                    }
                }
            }

            if(!isContained){
                if(scanDevice.getRssi() > mTopOneDevice.getRssi()){
                    lockList.add(0,scanDevice);
                }else {
                    lockList.add(scanDevice);
                }
            }

        }else {
            lockList.add(scanDevice);
        }

    }

    /**
     * the lock mode will be changed,so should update the list when lock mode changed.
     * @param scanDevice the lock that be discovered.
     */
    private void removeOtherStatusLock(ExtendedBluetoothDevice scanDevice, LinkedList<ExtendedBluetoothDevice> lockList){
        if(!lockList.isEmpty()){
            int length = lockList.size();
            for(int i = 0; i < length ; i++){
                ExtendedBluetoothDevice device = lockList.get(i);
                if(device.getAddress().equals(scanDevice.getAddress())){
                    lockList.remove(i);
                    length --;
                }else {
                    if(System.currentTimeMillis() - device.getDate() >= TIMEOUT) {
                        lockList.remove(i);
                        length --;
                    }
                }
            }
        }
    }

    @Override
    public DeviceViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View mView = LayoutInflater.from(mContext).inflate(com.syriasoft.hotelservices.R.layout.lock_add_list_item, parent, false);
        DeviceViewHolder holder= new DeviceViewHolder(mView);
        return holder;
    }

    @Override
    public void onBindViewHolder(DeviceViewHolder _holder, final int position) {
        //final ExtendedBluetoothDevice item = mDataList.get(position);
        _holder.t.setText( mDataList.get(position).getName());
        _holder.b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ToastMaker.MakeToast("--start init lock--" ,mContext );
                // if you need to add a hotel lock you should set hotel data for lock init.

//        HotelData hotelData = new HotelData();
//        hotelData.setBuildingNumber(mBuildingNumber);
//        hotelData.setFloorNumber(mFloorNumber);
//        hotelData.setHotelInfo(mHotelInfoStr);
//        try {
//            device.setHotelData(hotelData);
//        } catch (ParamInvalidException e){
//
//        }


                /**
                 * lockData: the server api lockData param need
                 * isNBLock: is a NB-IoT lock.
                 */
                TTLockClient.getDefault().initLock(mDataList.get(position), new InitLockCallback() {
                    @Override
                    public void onInitLockSuccess(String lockData) {
                        //this must be done after lock is initialized,call server api to post to your server
                        if(FeatureValueUtil.isSupportFeature(lockData, FeatureValue.NB_LOCK)){
                            ScanLockActivity.setNBServerForNBLock(lockData,mDataList.get(position).getAddress());
                        }else {
                            ToastMaker.MakeToast("--lock is initialized success--",mContext);
                            ScanLockActivity.upload2Server(lockData);
                        }


                    }

                    @Override
                    public void onFail(LockError error) {
                        ToastMaker.MakeToast(error.getErrorMsg()+"error lock",mContext);
                    }
                });
            }
        });
    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    class DeviceViewHolder extends RecyclerView.ViewHolder {

        TextView t ;
        Button b ;

        public DeviceViewHolder(View itemView){
            super(itemView);
            t = (TextView)itemView.findViewById(com.syriasoft.hotelservices.R.id.tv_lock_name) ;

            b=(Button) itemView.findViewById(com.syriasoft.hotelservices.R.id.lock_init);
        }

    }

}
