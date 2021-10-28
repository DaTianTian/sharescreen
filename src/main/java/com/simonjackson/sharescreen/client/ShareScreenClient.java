package com.simonjackson.sharescreen.client;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * 屏幕共享客户端
 * @author SimonJackson
 * */
public class ShareScreenClient {
    private static int width  =200;
    private static int height = 100;
    private static int size = 4;
    private static int port = 12345;
    private static Map<Integer,JLabel> map = new HashMap<>();
    public static void main(String[] args)  {
        try {
            DatagramSocket ds = new DatagramSocket(port);
            init(ds);//先接收服务器屏幕的尺寸，以初始化窗口尺寸，并初始化每个区域的jLable
            receiveImageDataFlow(ds);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private static void init(DatagramSocket ds) throws IOException {
        JFrame jFrame = new JFrame();
        jFrame.setLayout(null);
        jFrame.setSize(new Dimension(width,height));
        jFrame.setTitle("synchronized_screen");
        jFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        jFrame.setVisible(true);
        byte[] bytes = new byte[1024];
        DatagramPacket dp = new DatagramPacket(bytes,bytes.length);
        ds.receive(dp);
        String[] str = new String(dp.getData(),0,dp.getLength()).split(",");
        width = Integer.valueOf(str[0]);
        height = Integer.valueOf(str[1]);

        for(int i=0;i<size*size;i++){
            JLabel jLabel=new JLabel();
            int x = width/size*(i/size);
            int y = height/size*(i%size);
            jLabel.setLocation(x,y);
            jLabel.setSize(width/size, height/size);
            jFrame.getContentPane().add(jLabel);
            map.put(i,jLabel);
        }
        jFrame.setSize(new Dimension(width,height));
        jFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        jFrame.setVisible(true);
    }
    private static void receiveImageDataFlow(DatagramSocket ds) throws IOException{
        boolean flag = false;
        while(true){
            //创建数据包接收数据
            byte[] bytes = new byte[65536];
            DatagramPacket dp = new DatagramPacket(bytes,bytes.length);
            ds.receive(dp);
            int index = bytes[0];//获取图片编号，以确定图片位置
            byte[] byteArr = Arrays.copyOfRange(bytes,1,bytes.length);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        display(map.get(index), ImageIO.read(new ByteArrayInputStream(byteArr)));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }

    private static void display(JLabel jLabel, BufferedImage image){
        if(image==null){
            return;
        }
        jLabel.setIcon(new ImageIcon(image));
        jLabel.setSize(image.getWidth(), image.getHeight());
    }
}
