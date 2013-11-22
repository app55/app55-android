package com.app55.android.example;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import android.test.AndroidTestCase;

import com.app55.android.example.console.Console;
import com.app55.domain.Address;
import com.app55.domain.Card;
import com.app55.domain.Transaction;
import com.app55.domain.User;
import com.app55.error.ApiException;
import com.app55.message.CardCreateResponse;
import com.app55.message.CardDeleteResponse;
import com.app55.message.CardListResponse;
import com.app55.message.ResponseListener;
import com.app55.message.TransactionCommitResponse;
import com.app55.message.TransactionCreateResponse;
import com.app55.message.UserCreateResponse;
import com.app55.message.UserGetResponse;

public class ExampleScript extends AndroidTestCase
{
	private Console	console;

	public ExampleScript(Console console)
	{
		this.console = console;
	}

	public void execute()
	{
		console.clear();
		createUser();
	}

	private void createUser()
	{
		final String email = "example." + getTimestamp() + "@javalibtester.com";
		final String phone = "0123 456 7890";
		String password = "pa55word";
		console.append("Creating a user...");

		Configuration.GATEWAY.createUser(new User(email, phone, password, password)).send(new ResponseListener<UserCreateResponse>() {

			@Override
			public void onResponse(UserCreateResponse response)
			{
				assertEquals("UserCreate: Unexpected email.", email, response.getUser().getEmail());
				assertEquals("UserCreate: Unexpected phone.", phone, response.getUser().getPhone());

				console.append("SUCCESS. User created.\nID: " + response.getUser().getId() + "\nEmail: " + response.getUser().getEmail());

				checkGetUser(response.getUser());
			}

			@Override
			public void onError(ApiException e)
			{
				console.append(e);
			}
		});
	}

	private void checkGetUser(final User user)
	{
		console.append("\nFetching User by ID: " + user.getId());

		User basicUser = new User();
		basicUser.setId(user.getId());

		Configuration.GATEWAY.getUser(basicUser).send(new ResponseListener<UserGetResponse>() {

			@Override
			public void onResponse(UserGetResponse response)
			{
				assertEquals("UserGet: Returned user not not as expected.", user.getId(), response.getUser().getId());
				console.append("SUCCESS. User returned.\nPhone: " + user.getPhone());

				createCard(response.getUser());
			}

			@Override
			public void onError(ApiException e)
			{
				console.append(e);
			}
		});
	}

	private void createCard(final User user)
	{
		Address address = new Address("8 Exchange Quay", "Manchester", "M5 3EJ", "GB");
		final Card card = new Card("App55 User", "4111111111111111", getTimestamp("MM/yyyy", 90), "111", null, address);

		console.append("\nCreating a new card. Number: " + card.getNumber());
		Configuration.GATEWAY.createCard(new User((long) user.getId()), card).send(new ResponseListener<CardCreateResponse>() {

			@Override
			public void onResponse(CardCreateResponse response)
			{
				assertEquals("CardCreate: Unexpected card expiry.", card.getExpiry(), response.getCard().getExpiry());
				assertNotNull("CardCreate: Unexpected card token.", response.getCard().getToken());
				console.append("SUCCESS. Card created.\nExpiry: " + card.getExpiry());

				listCards(user, 1);
			}

			@Override
			public void onError(ApiException e)
			{
				console.append(e);
			}
		});
	}

	private void listCards(final User user, final int expectedNumber)
	{
		console.append("\nListing all user cards. User ID: " + user.getId());
		Configuration.GATEWAY.listCards(new User((long) user.getId())).send(new ResponseListener<CardListResponse>() {

			@Override
			public void onResponse(CardListResponse response)
			{
				assertTrue("CardList: Incorrect card list size.", response.getCards().size() == expectedNumber);
				console.append("SUCCESS. List of cards returned.\nList size: " + response.getCards().size() + "\nFirst card token: "
						+ response.getCards().get(0).getToken());

				createTransaction(user, response.getCards().get(0));
			}

			@Override
			public void onError(ApiException e)
			{
				console.append(e);
			}
		});
	}

	private void createTransaction(final User user, final Card card)
	{
		final Transaction transaction = new Transaction("0.10", "GBP", null);
		Card c = new Card(card.getToken());
		console.append("\nCreating a transaction: " + transaction.getAmount() + transaction.getCurrency());

		Configuration.GATEWAY.createTransaction(new User((long) user.getId()), c, transaction).send(new ResponseListener<TransactionCreateResponse>() {

			@Override
			public void onResponse(TransactionCreateResponse response)
			{
				assertEquals("TransactionCreate: Unexpected amount.", transaction.getAmount(), response.getTransaction().getAmount());
				assertEquals("TransactionCreate: Unexpected transaction code.", "succeeded", response.getTransaction().getCode());
				console.append("SUCCESS. Transaction created.\nAuth Code: " + response.getTransaction().getAuthCode());

				commitTransaction(response.getTransaction(), user, card);
			}

			@Override
			public void onError(ApiException e)
			{
				console.append(e);
			}
		});
	}

	private void commitTransaction(Transaction transaction, final User user, final Card card)
	{
		console.append("\nCommitting Transaction: " + transaction.getAmount());

		Configuration.GATEWAY.commitTransaction(new Transaction(transaction.getId())).send(new ResponseListener<TransactionCommitResponse>() {

			@Override
			public void onResponse(TransactionCommitResponse response)
			{
				assertEquals("TransactionCommit: Unexpected transaction code.", "succeeded", response.getTransaction().getCode());
				assertNotNull("TransactionCommit: Unexpected transaction auth code.", response.getTransaction().getAuthCode());
				console.append("SUCCESS. Transaction committed.\nCode: " + response.getTransaction().getCode());

				deleteCard(user, card);
			}

			@Override
			public void onError(ApiException e)
			{
				console.append(e);
			}
		});
	}

	private void deleteCard(User user, Card card)
	{
		console.append("\nDeleting a card: " + card.getToken());

		Configuration.GATEWAY.deleteCard(new User((long) user.getId()), new Card(card.getToken())).send(new ResponseListener<CardDeleteResponse>() {

			@Override
			public void onResponse(CardDeleteResponse response)
			{
				assertNotNull("CardDelete: Response not returned.", response);
				console.append("SUCCESS. Card deleted.");
			}

			@Override
			public void onError(ApiException e)
			{
				console.append(e);
			}
		});
	}

	public static String getTimestamp(String format, Integer daysInFuture)
	{
		Calendar c = Calendar.getInstance();
		if (daysInFuture != null)
			c.add(Calendar.DAY_OF_YEAR, daysInFuture);
		return new SimpleDateFormat(format, Locale.getDefault()).format(c.getTime());
	}

	public static String getTimestamp()
	{
		return getTimestamp("yyyyMMddHHmmss", null);
	}
}