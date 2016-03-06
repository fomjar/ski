package com.ski.stub;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;

import fomjar.server.FjMessageQueue;
import fomjar.server.FjMessageWrapper;
import fomjar.server.FjReceiver;
import fomjar.server.FjSender;
import fomjar.server.FjServer;
import fomjar.server.FjServer.FjServerTask;
import fomjar.server.FjServerToolkit;

public class UiSender extends JFrame {

    private static final long serialVersionUID = 1695489211182536137L;
    private static final String DEFAULT_MESSAGE = "{\"fs\":\"stub\", \"ts\": \"wa\", \"sid\":\"00000000\", \"inst\":768, \"args\":null}";
    private FjSender  sender;
    private JTextArea jta_input;
    private JTextArea jta_output;
    private JButton   jbt_send;
    
    public UiSender() {
        setTitle("SKI-STUB");
        setSize(800, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                        new JScrollPane(jta_input = new JTextArea(DEFAULT_MESSAGE)),
                        new JScrollPane(jta_output = new JTextArea())),
                BorderLayout.CENTER);
        getContentPane().add(jbt_send = new JButton("SEND"), BorderLayout.SOUTH);
        jbt_send.addActionListener((e)->{
            sender.send(FjServerToolkit.createMessage(jta_input.getText()));
        });
        ((JComponent) jta_input.getParent().getParent()).setBorder(BorderFactory.createTitledBorder("INPUT"));
        ((JComponent) jta_output.getParent().getParent()).setBorder(BorderFactory.createTitledBorder("OUTPUT"));
        jta_input.setFont(jta_output.getFont().deriveFont(18.0f));
        jta_output.setFont(jta_output.getFont().deriveFont(18.0f));
        
        FjMessageQueue  mq = new FjMessageQueue();
        FjReceiver      receiver = new FjReceiver(mq, 3003);
        FjServer        server   = new FjServer("stub", mq);
        server.addServerTask(new FjServerTask() {
            @Override
            public void onMessage(FjServer server, FjMessageWrapper wrapper) {
                jta_output.append(wrapper.message().toString() + "\n");
            }
        });
        new Thread(receiver, "stub-fjreceiver").start();
        new Thread(server,   "stub-fjserver")  .start();
        sender = new FjSender();
        new Thread(sender, "stub-fjsender").start();
    }
    
}
