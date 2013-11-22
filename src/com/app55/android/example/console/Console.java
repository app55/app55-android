package com.app55.android.example.console;

import java.io.PrintWriter;
import java.io.StringWriter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

public class Console extends BroadcastReceiver
{
	private static final String	ACTION_APPEND	= "com.example.app55.console.ACTION_APPEND";

	private ConsoleUIHandler	consoleUIHandler;
	private Context				applicationContext;

	public Console(ConsoleUIHandler consoleUIHandler, Context applicationContext)
	{
		this.consoleUIHandler = consoleUIHandler;
		this.applicationContext = applicationContext;
	}

	public void register(Context context)
	{
		IntentFilter filter = new IntentFilter(ACTION_APPEND);
		context.registerReceiver(this, filter);
	}

	public void unregister(Context context)
	{
		context.unregisterReceiver(this);
	}

	public void append(Throwable t)
	{
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		t.printStackTrace(pw);
		append(sw.toString());
	}

	public void append(String text)
	{
		sendBroadcast(text);
	}

	public void clear()
	{
		sendBroadcast("");
	}

	@Override
	public void onReceive(Context context, Intent intent)
	{
		String text = intent.getStringExtra("text");
		consoleUIHandler.append(text);
	}

	private void sendBroadcast(String text)
	{
		Intent intent = new Intent();
		intent.putExtra("text", text);
		intent.setAction(ACTION_APPEND);
		applicationContext.sendBroadcast(intent);
	}
}