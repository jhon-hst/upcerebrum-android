package com.jhonhst.upcerebrum.games.ballSort

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun BallSort(){
    ContentBallSort()
}

@Composable
fun ContentBallSort(){
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)
    ) {

        BallSortTube()
    }
}

@Composable
fun BallSortTube(){
    Box(
        modifier = Modifier
            .size(width = 60.dp, height = 200.dp)
            .border(width= 2.dp, color = MaterialTheme.colorScheme.outline)


    ) {


    }
}