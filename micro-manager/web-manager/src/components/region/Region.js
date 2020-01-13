/*
 * MIT License
 *
 * Copyright (c) 2020 micro-manager
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

import React from 'react';
import M from 'materialize-css';
import $ from 'jquery';
import Utils from '../../utils';
import MainLayout from '../shared/MainLayout';
import RegionCard from "./RegionCard";

export default class Regions extends React.Component {
  constructor (props) {
    super(props);
    this.state = { data: [], loading: false, showAdd: true };
  }

  componentDidUpdate = () => {
    if (this.state.data.length > 0) {
      const lastIndex = this.state.data.length - 1;
      if (this.state.data[lastIndex].id === 0) {
        const offset = $('#region' + lastIndex).offset().top;
        $(window).scrollTop(offset);
        this.state.tooltipInstances[0].destroy();
      }
    }
  };

  componentDidMount = () => {
    this.loadRegions();
    const instances = M.Tooltip.init(document.querySelectorAll('.tooltipped'));
    this.setState({ tooltipInstances: instances });
  };

  componentWillUnmount = () => {
    this.state.tooltipInstances[0].destroy();
  };

  updateNewRegion = isCancel => {
    const newData = this.state.data;
    if (isCancel) {
      newData.splice(newData.length - 1, 1);
      this.setState({ data: newData, showAdd: true });
    } else {
      this.setState({ showAdd: true });
    }
    const instances = M.Tooltip.init(document.querySelectorAll('.tooltipped'));
    this.setState({ tooltipInstances: instances });
  };

  addRegion = () => {
    const newRegion = {
      id: 0, regionName: '', regionDescription: '', active: ''
    };
    const newData = this.state.data;
    newData.push(newRegion);
    this.setState({ data: newData, showAdd: false });
  };

  loadRegions = () => {
    this.setState({ loading: true });
    const self = this;
    Utils.ajaxGet('/regions',
      function (data) {
        self.setState({ data: data, loading: false });
      });
  };

  render = () => {
    let regionNodes;
    const self = this;
    if (this.state.data) {
      regionNodes = this.state.data.map(function (region, index) {
        return (
          <RegionCard key={index} index={index} region={region} updateNewRegion={self.updateNewRegion} onRemove={self.loadRegions}/>
        );
      });
    }
    return (
      <MainLayout title='Regions'>
        {regionNodes}
        <div className="fixed-action-btn tooltipped" data-position="left" data-tooltip="Add region">
          <button disabled={!this.state.showAdd} className="waves-effect waves-light btn-floating btn-large grey darken-4" onClick={this.addRegion}>
            <i className="large material-icons">add</i>
          </button>
        </div>
      </MainLayout>
    );
  };
}