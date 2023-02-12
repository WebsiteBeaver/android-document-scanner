# Android Document Scanner

This is an Android library that lets you scan documents. You can use it to create
apps that let users scan notes, homework, business cards, receipts, or anything with a rectangular shape.

![Dollar Android](https://user-images.githubusercontent.com/26162804/160306955-af9c5dd6-5cdf-4e2c-8770-c734a594985d.gif)

## Install

Open `build.gradle` and add this to `dependencies`

```bash
implementation 'com.websitebeaver:documentscanner:1.3.4'
```

## Examples

* [Basic Example](#basic-example)
* [Limit Number of Scans](#limit-number-of-scans)
* [Remove Cropper](#remove-cropper)
* [Java Example](#java-example)

### Basic Example

```kotlin
package com.your.project

import com.websitebeaver.documentscanner.utils.ImageUtil
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.websitebeaver.documentscanner.DocumentScanner

class MainActivity : AppCompatActivity() {
    private lateinit var croppedImageView: ImageView

    private val documentScanner = DocumentScanner(
        this,
        { croppedImageResults ->
            // display the first cropped image
            croppedImageView.setImageBitmap(
                ImageUtil().readBitmapFromFileUriString(
                    croppedImageResults.first(),
                    contentResolver
                )
            )
        },
        {
            // an error happened
            errorMessage -> Log.v("documentscannerlogs", errorMessage)
        },
        {
            // user canceled document scan
            Log.v("documentscannerlogs", "User canceled document scan")
        }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // cropped image
        croppedImageView = findViewById(R.id.cropped_image_view)

        // start document scan
        documentScanner.startScan()
    }
}
```

Here's what this example looks like with several items

<video src="https://user-images.githubusercontent.com/26162804/160264222-bef1ba3d-d6c1-43c8-ba2e-77ff5baef836.mp4" data-canonical-src="https://user-images.githubusercontent.com/26162804/160264222-bef1ba3d-d6c1-43c8-ba2e-77ff5baef836.mp4" controls="controls" muted="muted" class="d-block rounded-bottom-2 border-top width-fit" style="max-height:640px;"></video>

<video src="https://user-images.githubusercontent.com/26162804/160695710-d30ec1ad-0179-4200-be9f-94dcd5d3b920.mp4" data-canonical-src="https://user-images.githubusercontent.com/26162804/160695710-d30ec1ad-0179-4200-be9f-94dcd5d3b920.mp4" controls="controls" muted="muted" class="d-block rounded-bottom-2 border-top width-fit" style="max-height:640px;"></video>

<video src="https://user-images.githubusercontent.com/26162804/160695805-992a0a66-a371-4783-afab-35366541afa4.mp4" data-canonical-src="https://user-images.githubusercontent.com/26162804/160695805-992a0a66-a371-4783-afab-35366541afa4.mp4" controls="controls" muted="muted" class="d-block rounded-bottom-2 border-top width-fit" style="max-height:640px;"></video>

<video src="https://user-images.githubusercontent.com/26162804/160695839-4494f71b-db5b-4db5-9de6-00e5ede210fa.mp4" data-canonical-src="https://user-images.githubusercontent.com/26162804/160695839-4494f71b-db5b-4db5-9de6-00e5ede210fa.mp4" controls="controls" muted="muted" class="d-block rounded-bottom-2 border-top width-fit" style="max-height:640px;"></video>

<video src="https://user-images.githubusercontent.com/26162804/160695861-0d807162-9649-481d-990d-8d031d1a3170.mp4" data-canonical-src="https://user-images.githubusercontent.com/26162804/160695861-0d807162-9649-481d-990d-8d031d1a3170.mp4" controls="controls" muted="muted" class="d-block rounded-bottom-2 border-top width-fit" style="max-height:640px;"></video>

### Limit Number of Scans

You can limit the number of scans. For example if your app lets a user scan a business
card you might want them to only capture the front and back. In this case you can set
maxNumDocuments to 2.

```kotlin
package com.your.project

import com.websitebeaver.documentscanner.utils.ImageUtil
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.websitebeaver.documentscanner.DocumentScanner
import com.websitebeaver.documentscanner.constants.ResponseType

class MainActivity : AppCompatActivity() {
    private lateinit var croppedImageView: ImageView

    private val documentScanner = DocumentScanner(
        this,
        { croppedImageResults ->
            // display the first cropped image
            croppedImageView.setImageBitmap(
                ImageUtil().readBitmapFromFileUriString(
                    croppedImageResults.first(),
                    contentResolver
                )
            )
        },
        {
            // an error happened
            errorMessage -> Log.v("documentscannerlogs", errorMessage)
        },
        {
            // user canceled document scan
            Log.v("documentscannerlogs", "User canceled document scan")
        },
        ResponseType.IMAGE_FILE_PATH,
        true,
        2
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // cropped image
        croppedImageView = findViewById(R.id.cropped_image_view)

        // start document scan
        documentScanner.startScan()
    }
}

```

<video src="https://user-images.githubusercontent.com/26162804/160695619-b1931101-9196-49de-b30f-f63c4af8ce9d.mp4" data-canonical-src="https://user-images.githubusercontent.com/26162804/160695619-b1931101-9196-49de-b30f-f63c4af8ce9d.mp4" controls="controls" muted="muted" class="d-block rounded-bottom-2 border-top width-fit" style="max-height:640px;"></video>

### Remove Cropper

You can automatically accept the detected document corners, and prevent the user from
making adjustments. Set letUserAdjustCrop to false to skip the crop screen. This limits
the max number of scans to 1.

```kotlin
package com.your.project

import com.websitebeaver.documentscanner.utils.ImageUtil
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.websitebeaver.documentscanner.DocumentScanner
import com.websitebeaver.documentscanner.constants.ResponseType

class MainActivity : AppCompatActivity() {
    private lateinit var croppedImageView: ImageView

    private val documentScanner = DocumentScanner(
        this,
        { croppedImageResults ->
            // display the first cropped image
            croppedImageView.setImageBitmap(
                ImageUtil().readBitmapFromFileUriString(
                    croppedImageResults.first(),
                    contentResolver
                )
            )
        },
        {
            // an error happened
            errorMessage -> Log.v("documentscannerlogs", errorMessage)
        },
        {
            // user canceled document scan
            Log.v("documentscannerlogs", "User canceled document scan")
        },
        ResponseType.IMAGE_FILE_PATH,
        false
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // cropped image
        croppedImageView = findViewById(R.id.cropped_image_view)

        // start document scan
        documentScanner.startScan()
    }
}
```

<video src="https://user-images.githubusercontent.com/26162804/160695550-7005f0cc-597d-4b96-8a6c-867d34fb6969.mp4" data-canonical-src="https://user-images.githubusercontent.com/26162804/160695550-7005f0cc-597d-4b96-8a6c-867d34fb6969.mp4" controls="controls" muted="muted" class="d-block rounded-bottom-2 border-top width-fit" style="max-height:640px;"></video>

### Java Example

Even though all of the examples so far have been in Kotlin, you can use
this library with Java.

```java
package com.your.project;

import com.websitebeaver.documentscanner.utils.ImageUtil;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import com.websitebeaver.documentscanner.DocumentScanner;

public class MainActivity extends AppCompatActivity {

    private ImageView croppedImageView;

    DocumentScanner documentScanner = new DocumentScanner(
            this,
        (croppedImageResults) -> {
            // display the first cropped image
            croppedImageView.setImageBitmap(
                ImageUtil().readBitmapFromFileUriString(
                    croppedImageResults.first(),
                    getContentResolver()
                )
            );
            return null;
        },
        (errorMessage) -> {
            // an error happened
            Log.v("documentscannerlogs", errorMessage);
            return null;
        },
        () -> {
            // user canceled document scan
            Log.v("documentscannerlogs", "User canceled document scan");
            return null;
        },
        null,
        null,
        null
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // cropped image
        croppedImageView = findViewById(R.id.cropped_image_view);

        // start document scan
        documentScanner.startScan();
    }
}
```

## License

Copyright 2022 David Marcus

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
