package com.example.butterknife;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import butterknife.Views;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import static com.example.butterknife.SimpleAdapter.ViewHolder;
import static org.fest.assertions.api.ANDROID.assertThat;

@RunWith(RobolectricTestRunner.class)
public class SimpleAdapterTest {
  @Test public void verifyViewHolderViews() {
    Context context = Robolectric.application;

    View root = LayoutInflater.from(context).inflate(R.layout.simple_list_item, null);
    ViewHolder holder = new ViewHolder(root);

    assertThat(holder.word).hasId(R.id.word);
    assertThat(holder.length).hasId(R.id.length);
    assertThat(holder.position).hasId(R.id.position);

    Views.reset(holder);
    assertThat(holder.word).isNull();
    assertThat(holder.length).isNull();
    assertThat(holder.position).isNull();
  }
}
