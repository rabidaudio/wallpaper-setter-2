package audio.rabid.rotatingwallpapers;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;

/**
 * Created by  charles  on 9/14/17.
 */

public class WallpaperChangerJobService extends JobService {
    @Override
    public boolean onStartJob(JobParameters params) {
        startService(new Intent(this, WallpaperChangerIntentService.class));
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }
}
