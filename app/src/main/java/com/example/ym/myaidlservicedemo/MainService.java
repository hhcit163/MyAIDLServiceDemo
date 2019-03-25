package com.example.ym.myaidlservicedemo;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by ym on 19-2-28.
 */

public class MainService extends Service {
    private final String TAG="MainService";

    private RemoteCallbackList<IOnNewBookArricedListener> mListeners=new RemoteCallbackList<>();

    private CopyOnWriteArrayList<Book> bookList;

    private AtomicBoolean isServicDestory=new AtomicBoolean(false);

    @Override
    public void onCreate() {
        super.onCreate();
        bookList=new CopyOnWriteArrayList<>();
        initData();
        new Thread(new ServiceWork()).start();
    }

    private void initData(){
        Book book1=new Book("三国演义");
        Book book2=new Book("水浒传");
        Book book3=new Book("红楼梦");
        Book book4=new Book("西游记");
        bookList.add(book1);
        bookList.add(book2);
        bookList.add(book3);
        bookList.add(book4);

    }
    public class IMyAidlInterface extends com.example.ym.myaidlservicedemo.IMyAidlInterface.Stub{
        @Override
        public List<Book> getBooklist() throws RemoteException {
            return bookList;
        }

        @Override
        public void addBookInOut(Book book) throws RemoteException {
            if (book!=null){
                book.setName("服务器修改新的书名 inout");
                bookList.add(book);
            }else{
                Log.e(TAG,"服务器接受一个空对象 inout");
            }

        }

        @Override
        public void addBookIn(Book book) throws RemoteException {
            if (book!=null){
                book.setName("服务器修改新的书名 in");
                bookList.add(book);
            }else{
                Log.e(TAG,"服务器接受一个空对象 in");
            }

        }

        @Override
        public void addBookOut(Book book) throws RemoteException {
            if (book!=null){
                book.setName("客户端修改新的书名 out");
                bookList.add(book);
            }else{
                Log.e(TAG,"客户端接受一个空对象 out");
            }

        }

        @Override
        public void registListener(IOnNewBookArricedListener listener) throws RemoteException {
            mListeners.register(listener);

        }

        @Override
        public void unregistListener(IOnNewBookArricedListener listener) throws RemoteException {
            mListeners.unregister(listener);

        }
    }

    public void onNewBook(Book book) throws RemoteException {
        bookList.add(book);
        int n= mListeners.beginBroadcast();
        for (int i=0;i<n;i++){
            IOnNewBookArricedListener listener=mListeners.getBroadcastItem(i);
            if (listener!=null){
                try {
                    listener.onNewBookArrived(book);
                }catch (RemoteException e){
                    e.printStackTrace();

                }

            }
        }
        mListeners.finishBroadcast();
    }

    private class ServiceWork implements Runnable{

        @Override
        public void run() {
            while (!isServicDestory.get()){
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                int i =bookList.size()+1;
                Book book=new Book("三国志"+i);
                try {
                    onNewBook(book);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }

            }
        }
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new IMyAidlInterface();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isServicDestory.set(true);
    }
}
