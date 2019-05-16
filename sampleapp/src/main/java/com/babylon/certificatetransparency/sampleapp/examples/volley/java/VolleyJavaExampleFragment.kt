/*
 * Copyright 2019 Babylon Partners Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.babylon.certificatetransparency.sampleapp.examples.volley.java

import com.babylon.certificatetransparency.sampleapp.R
import com.babylon.certificatetransparency.sampleapp.examples.BaseExampleFragment

class VolleyJavaExampleFragment : BaseExampleFragment<VolleyJavaExampleViewModel>() {

    override fun getViewModelClass(): Class<VolleyJavaExampleViewModel> = VolleyJavaExampleViewModel::class.java

    override fun getTitle() = R.string.volley_java_example
}