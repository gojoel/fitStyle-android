package ai.folded.fitstyle.glide;

import static ai.folded.fitstyle.utils.ConstantsKt.URL_FETCHER_TIMEOUT;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.data.HttpUrlFetcher;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.signature.ObjectKey;

import java.io.InputStream;
import java.net.URL;

import ai.folded.fitstyle.data.StyledImage;
import ai.folded.fitstyle.utils.AwsUtils;

public class S3ImageLoader implements ModelLoader<StyledImage, InputStream> {

    @Nullable
    @Override
    public LoadData<InputStream> buildLoadData(@NonNull StyledImage styledImage, int width, int height, @NonNull Options options) {
        final URL imageUrl = AwsUtils.INSTANCE.generateUrl(styledImage.imageKey());
        if (imageUrl == null) {
            return null;
        }

        return new LoadData<>(new ObjectKey(styledImage), new HttpUrlFetcher(new GlideUrl(imageUrl), URL_FETCHER_TIMEOUT));
    }

    @Override
    public boolean handles(@NonNull StyledImage styledImage) {
        return true;
    }
}
