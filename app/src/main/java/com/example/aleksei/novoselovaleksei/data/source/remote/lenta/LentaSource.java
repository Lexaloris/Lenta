package com.example.aleksei.novoselovaleksei.data.source.remote.lenta;

import android.support.annotation.NonNull;

import com.example.aleksei.novoselovaleksei.data.Tiding;
import com.example.aleksei.novoselovaleksei.data.source.TidingDataSource;
import com.example.aleksei.novoselovaleksei.data.source.remote.common.BaseSource;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

import static com.example.aleksei.novoselovaleksei.data.source.remote.lenta.RssRetrofitAdapter.RSS_LENTA_LINK;

public class LentaSource extends BaseSource {

    public LentaSource() {
        super(RSS_LENTA_LINK);
    }

    @Override
    protected Call execute(Retrofit retrofit, @NonNull final TidingDataSource.RemoteLoadTidingsCallback callback) {

        RssRetrofitAdapter retrofitService = retrofit.create(RssRetrofitAdapter.class);
        Call<RssLentaFeed> call = retrofitService.getItems();
        call.enqueue(new Callback<RssLentaFeed>() {
            @Override
            public void onResponse(Call<RssLentaFeed> call, Response<RssLentaFeed> response) {
                RssLentaFeed feed = response.body();
                List<RssLentaItem> mItems = feed.getChannel().getLentaItemList();

                LentaSource source = new LentaSource();
                List<Tiding> tidings = source.getTidings(mItems);
                callback.onRemoteTidingLoaded(tidings);
            }

            @Override
            public void onFailure(Call<RssLentaFeed> call, Throwable t) {
                callback.onRemoteDataNotAvailable();
            }
        });
        return call;
    }

    @Override
    public Source getSource() {
        return Source.LENTA;
    }

    private List<Tiding> getTidings(List<RssLentaItem> mItems) {
        List<Tiding> tidings = new ArrayList<>();
        for (RssLentaItem item: mItems) {
            String title = item.getTitle();
            String publicationDate = item.getPublicationDate();
            String description = item.getDescription();
            Enclosure enclosure = item.getEnclosure();
            String imageUrl = null;
            if (enclosure != null)  {
                imageUrl = enclosure.getUrl();
            }
            tidings.add(new Tiding(title, publicationDate, description, imageUrl, getSource()));
        }
        return tidings;
    }
}
