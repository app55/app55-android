package com.app55.android.example.console;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.TextView;

public class ConsoleUIHandler extends Handler
{
	private TextView	consoleView;

	public ConsoleUIHandler(TextView consoleView, Bundle savedInstanceState)
	{
		this.consoleView = consoleView;
		processSavedState(savedInstanceState);
	}

	public void handleMessage(Message msg)
	{
		String consoleText = consoleView.getText().toString();
		String textToAppend = msg.getData().getString("text");
		
		if (textToAppend.length() > 0)
			consoleText += textToAppend;
		else
			consoleText = "";

		if (consoleText.length() > 0)
			consoleText += "\n";
		
		consoleView.setText(consoleText);
	}

	public void append(String text)
	{
		Message m = new Message();
		Bundle d = new Bundle();
		d.putString("text", text);
		m.setData(d);

		sendMessage(m);
	}

	public void onSaveInstanceState(Bundle outState)
	{
		outState.putString("text", consoleView.getText().toString());
	}

	private void processSavedState(Bundle savedInstanceState)
	{
		if (savedInstanceState != null)
			consoleView.setText(savedInstanceState.getString("text"));
	}
}