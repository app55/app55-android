package com.app55.android.example;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.app55.android.example.console.Console;
import com.app55.android.example.console.ConsoleUIHandler;

public class App55Activity extends Activity
{
	private Console				console;
	private ConsoleUIHandler	consoleUIHandler;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.console_view);

		TextView consoleView = (TextView) findViewById(R.id.consoleOutput);
		consoleUIHandler = new ConsoleUIHandler(consoleView, savedInstanceState);
		console = new Console(consoleUIHandler, getApplicationContext());
		console.register(this);
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		console.unregister(this);
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item)
	{
		switch (item.getItemId())
		{
			case R.id.action_run:
				runScript();
				return true;
			default:
				return false;
		}
	}

	private void runScript()
	{
		new ExampleScript(console).execute();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.menu, menu);
		return true;
	}

	@Override
	protected void onSaveInstanceState(Bundle outState)
	{
		consoleUIHandler.onSaveInstanceState(outState);
	}
}