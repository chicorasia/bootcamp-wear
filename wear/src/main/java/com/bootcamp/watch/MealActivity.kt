package com.bootcamp.watch

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.wearable.activity.ConfirmationActivity
import android.util.Log
import br.com.chicorialabs.shared.Meal
import com.bootcamp.watch.databinding.ActivityMealBinding
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.wearable.PutDataRequest
import com.google.android.gms.wearable.Wearable
import com.google.gson.Gson

class MealActivity : Activity(), GoogleApiClient.ConnectionCallbacks {

    private lateinit var client: GoogleApiClient
    private var currentMeal: Meal? = null
    private lateinit var binding: ActivityMealBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMealBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        client = GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .build()
        client.connect()

        binding.star.setOnClickListener {
            sendLike()
        }

    }

    private fun sendLike() {
        currentMeal?.let {
            val bytes = Gson().toJson(it.copy(favorited = true)).toByteArray()
            Wearable.DataApi.putDataItem(client, PutDataRequest.create("/liked").setData(bytes))
        }
        showConfirmationScreen()
    }

    private fun showConfirmationScreen() {
        val intent = Intent(this, ConfirmationActivity::class.java)
        intent.putExtra(ConfirmationActivity.EXTRA_ANIMATION_TYPE,
                ConfirmationActivity.SUCCESS_ANIMATION)
        intent.putExtra(ConfirmationActivity.EXTRA_MESSAGE,
                getString(R.string.starred_meal))
        startActivity(intent)
    }

    override fun onConnected(p0: Bundle?) {
        Wearable.MessageApi.addListener(client) { messageEvent ->
            currentMeal = Gson().fromJson(String(messageEvent.data), Meal::class.java)
            updateView()
        }

    }

    private fun updateView() {
        Log.i("wear-meal", "updateView() chamado!")
        currentMeal?.let {
            binding.mealTitle.text = it.title
            binding.calories.text = "${it.calories} calories"
            binding.ingredients.text = it.ingredients.joinToString(separator = ", ")

        }
    }


    override fun onConnectionSuspended(p0: Int) {
        Log.w("wear-meal", "onConnectionSuspended: Conexão suspensa")
    }
}
