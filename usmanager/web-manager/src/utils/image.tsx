/*
 * MIT License
 *
 * Copyright (c) 2020 usmanager
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

import {
  FaAddressCard,
  FaClock,
  FaCodeBranch,
  FaDatabase,
  FaDocker,
  FaGlobeAmericas,
  FaGlobeEurope,
  FaIdBadge,
  FaMapMarkedAlt,
  FaMapPin,
  FaMinus,
  FaTerminal,
  FaThumbsDown,
  FaThumbsUp,
  FaMapMarkerAlt,
  FaHdd,
  FaSdCard,
  FaTasks,
  FaPowerOff,
  FaSortAmountDown,
  FaGlobe,
  FaToolbox,
  FaListUl,
  FaThList,
  FaList,
  FaSuperpowers,
  FaGreaterThanEqual,
  FaLessThanEqual,
  FaHourglassHalf, FaBan
} from "react-icons/all";
import {FaDoorOpen} from "react-icons/all";
import {FaDoorClosed} from "react-icons/all";
import React from "react";
import {IRegion} from "../routes/region/Region";
import {IState} from "../routes/hosts/cloud/CloudHost";

// https://fontawesome.com/icons?d=gallery&m=free
// https://materializecss.com/icons.html
export const mapLabelToMaterialIcon = (label: string, value: any): string | JSX.Element => {
  label = label.toLowerCase();
  if (label === 'defaultdb') {
    return <FaDatabase/>;
  }
  if (label === 'id' || label === 'containerid' || label === 'instanceid') {
    return "fingerprint";
  }
  if (label === 'state') {
    switch (value) {
      case 'ready': return <FaThumbsUp/>;
      case 'down': return <FaThumbsDown/>;
      case 'drain': return <FaMinus/>;
    }
  }
  if (label === 'username') {
    return "account_circle";
  }
  if (label === 'region') {
    if (value === undefined) {
      return <FaMapMarkerAlt/>;
    }
    const region = value as IRegion;
    if (region.name.includes('us')) {
      return <FaGlobeAmericas/>;
    }
    if (region.name.includes('eu')) {
      return <FaGlobeEurope/>;
    }
  }
  if (label === 'country' || label === 'placement') {
    return <FaMapMarkedAlt/>;
  }
  if (label === 'city') {
    return <FaMapPin/>;
  }
  if (label.includes('privateip')) {
    return <FaIdBadge/>;
  }
  if (label.includes('publicip')) {
    return <FaAddressCard/>;
  }
  if (label.includes('time')) {
    return "access_time";
  }
  if (label.includes('date')) {
    return "date_range";
  }
  if (label.includes('command')) {
    return <FaTerminal/>;
  }
  if (label.includes('host')) {
    return 'devices';
  }
  if (label.includes('file')) {
    return 'file_upload';
  }
  if (label.includes('name')) {
    return "text_fields";
  }
  if (label.includes('external')) {
    return <FaDoorOpen/>;
  }
  if (label.includes('internal')) {
    return <FaDoorClosed/>;
  }
  if (label.includes('memory')) {
    return 'memory';
  }
  if (label.includes('label')) {
    return 'label';
  }
  if (label.includes('service')) {
    return 'layers';
  }
  if (label.includes('docker')) {
    return <FaDocker/>;
  }

  if (label.includes('password')) {
    return 'vpn_key';
  }
  if (label.includes('version')) {
    return <FaCodeBranch/>;
  }
  if (label === 'created') {
    return <FaClock/>;
  }
  if (label === 'image') {
    return <FaDocker/>;
  }
  if (label === 'minreplicas' || label === 'maxreplicas') {
    return 'format_list_numbered';
  }
  if (label === 'instancetype') {
    return <FaHdd/>;
  }
  if (label === 'imageid') {
    return <FaSdCard/>;
  }
  if (label === 'role') {
    if (value === 'MANAGER') {
      return <FaTasks/>;
    }
    if (value === 'WORKER') {
      return <FaToolbox/>;
    }
    return 'details';
  }
  if (label === 'availability') {
    return <FaPowerOff/>;
  }
  if (label === 'priority') {
    return <FaSortAmountDown/>;
  }
  if (label === 'decision') {
    return 'linear_scale';
  }
  if (label === 'generic') {
    return <FaGlobe/>;
  }
  if (label === 'field') {
    return <FaListUl/>;
  }
  if (label === 'operator') {
    return <FaThList/>;
  }
  if (label === 'valuemode') {
    return <FaList/>;
  }
  if (label === 'value' || label === 'description') {
    return 'short_text';
  }
  if (label === 'override') {
    return <FaSuperpowers/>;
  }
  if (label === 'minimumvalue') {
    return <FaGreaterThanEqual/>;
  }
  if (label === 'maximumvalue') {
    return <FaLessThanEqual/>;
  }
  if (label === 'state') {
    console.log(value)
    const state = value as IState;
    switch (state.name) {
      case 'running': return 'check';
      case 'pending':
      case 'stopping': return <FaHourglassHalf/>;
      case 'stopped': return <FaBan/>;
      case 'shutting_down': return '';
    }
  }
  if (value === true) {
    return "check";
  }
  if (value === false) {
    return "clear";
  }
  return "";
}
