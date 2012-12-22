/*
Copyright 2007 Flaptor (flaptor.com) 

Licensed under the Apache License, Version 2.0 (the "License"); 
you may not use this file except in compliance with the License. 
You may obtain a copy of the License at 

    http://www.apache.org/licenses/LICENSE-2.0 

Unless required by applicable law or agreed to in writing, software 
distributed under the License is distributed on an "AS IS" BASIS, 
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
See the License for the specific language governing permissions and 
limitations under the License.
*/
 
/**
 * This package provides a histogram that adapts to any data distribution. <p>
 * It has the following features: <p>
 * <ul>
 * <li> It adapts to any data distribution, keeping a more or less constant resolution throughout the data range by increasing the resolution where the data is more dense.
 * <li> It can process large amounts of data with a very small memory footprint. 
 * <li> It doesn't need pre- or post-processing to deliver statistics about the data seen so far.
 * </ul>
 * <p>
 * The following statistics are available:<p>
 * <ul>
 * <li> The accumulative density function for a given data point.
 * <li> The data point that splits the data set at a given percentile.
 * </ul>
 * <p>
 * @author Jorge Handl
 */

package com.flaptor.hist4j;
