package app.fangyi.apkextractor;

import android.app.AlertDialog;
import android.content.pm.PackageInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.Collator;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private RecyclerView packageInfoRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        packageInfoRecyclerView = findViewById(R.id.packageInfoRecyclerView);

        List<PackageInfo> packageInfoList = getPackageManager().getInstalledPackages(0);
        Collections.sort(packageInfoList, new Comparator<PackageInfo>() {
            public int compare(PackageInfo packageInfo1, PackageInfo packageInfo2) {
                String appName1 = packageInfo1.applicationInfo.loadLabel(getPackageManager()).toString();
                String appName2 = packageInfo2.applicationInfo.loadLabel(getPackageManager()).toString();
                return Collator.getInstance(Locale.CHINA).compare(appName1,appName2);
            }
        });
        PackageInfoAdapter packageInfoAdapter = new PackageInfoAdapter(packageInfoList);
        packageInfoRecyclerView.setAdapter(packageInfoAdapter);
        packageInfoRecyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        packageInfoRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
    }

    public class PackageInfoAdapter extends RecyclerView.Adapter<PackageInfoAdapter.ViewHolder> {
        private List<PackageInfo> localPackageInfoList;

        public class ViewHolder extends RecyclerView.ViewHolder {
            private ImageView appIconImageView;
            private TextView appNameTextView, packageNameTextView;

            public ViewHolder(View view) {
                super(view);
                appIconImageView = view.findViewById(R.id.appIconImageView);
                appNameTextView = view.findViewById(R.id.appNameTextView);
                packageNameTextView = view.findViewById(R.id.packageNameTextView);
            }
        }

        public PackageInfoAdapter(List<PackageInfo> packageInfoList) {
            localPackageInfoList = packageInfoList;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.package_info_item, viewGroup, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder viewHolder, final int position) {
            viewHolder.appIconImageView.setImageDrawable(localPackageInfoList.get(position).applicationInfo.loadIcon(getPackageManager()));
            viewHolder.appNameTextView.setText(localPackageInfoList.get(position).applicationInfo.loadLabel(getPackageManager()).toString());
            viewHolder.packageNameTextView.setText(localPackageInfoList.get(position).packageName);
            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        builder.setMessage("apk已提取至:" + extract(localPackageInfoList.get(position)))
                                .setPositiveButton("知道了", null)
                                .create().show();
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(MainActivity.this, "提取失败", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return localPackageInfoList.size();
        }
    }

    String extract(PackageInfo packageInfo) throws IOException {
        File source = new File(packageInfo.applicationInfo.sourceDir);
        File destination = new File(getExternalFilesDir(""), packageInfo.applicationInfo.loadLabel(getPackageManager()).toString() + ".apk");
        FileInputStream inputStream = new FileInputStream(source);
        FileOutputStream outputStream = new FileOutputStream(destination);
        FileChannel inputChannel = inputStream.getChannel();
        FileChannel outputChannel = outputStream.getChannel();
        inputChannel.transferTo(0, inputChannel.size(), outputChannel);
        inputStream.close();
        outputStream.close();
        return destination.toString();
    }
}