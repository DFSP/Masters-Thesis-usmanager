import React, { useRef, useLayoutEffect } from 'react';
import logo from './logo.svg';
import './App.css';
import * as am4core from "@amcharts/amcharts4/core";
import * as am4maps from "@amcharts/amcharts4/maps";
import am4geodata_worldLow from "@amcharts/amcharts4-geodata/worldLow";
import am4themes_animated from "@amcharts/amcharts4/themes/animated";

am4core.useTheme(am4themes_animated);

function App(props) {

  useLayoutEffect(() => {
    /**
     * ---------------------------------------
     * This demo was created using amCharts 4.
     *
     * For more information visit:
     * https://www.amcharts.com/
     *
     * Documentation is available at:
     * https://www.amcharts.com/docs/v4/
     * ---------------------------------------
     */

    // Themes begin
    am4core.useTheme(am4themes_animated);
    // Themes end

    // Create map instance
    var chart = am4core.create("chartdiv", am4maps.MapChart);

    // Set map definition
    chart.geodata = am4geodata_worldLow;

    // Set projection
    chart.projection = new am4maps.projections.Miller();

    // Create map polygon series
    var polygonSeries = chart.series.push(new am4maps.MapPolygonSeries());

    // Exclude Antartica
    polygonSeries.exclude = ["AQ"];

    // Make map load polygon (like country names) data from GeoJSON
    polygonSeries.useGeodata = true;

    // Configure series
    var polygonTemplate = polygonSeries.mapPolygons.template;
   // polygonTemplate.tooltipText = "{name}";
    polygonTemplate.polygon.fillOpacity = 0.6;


    // Create hover state and set alternative fill color
    //var hs = polygonTemplate.states.create("hover");
    //hs.properties.fill = chart.colors.getIndex(0);

    // Add image series
    var imageSeries = chart.series.push(new am4maps.MapImageSeries());
    imageSeries.mapImages.template.propertyFields.longitude = "longitude";
    imageSeries.mapImages.template.propertyFields.latitude = "latitude";
    imageSeries.mapImages.template.tooltipText = "{title}";
    imageSeries.mapImages.template.propertyFields.url = "url";

    var circle = imageSeries.mapImages.template.createChild(am4core.Circle);
    circle.radius = 5;
    circle.propertyFields.fill = "color";

    var circle2 = imageSeries.mapImages.template.createChild(am4core.Circle);
    circle2.radius = 5;
    circle2.propertyFields.fill = "color";


    circle2.events.on("inited", function(event){
      animateBullet(event.target);
    })


    function animateBullet(circle) {
      var animation = circle.animate([{ property: "scale", from: 1, to: 5 }, { property: "opacity", from: 1, to: 0 }], 1000, am4core.ease.circleOut);
      animation.events.on("animationended", function(event){
        animateBullet(event.target.object);
      })
    }

    var colorSet = new am4core.ColorSet();




    imageSeries.data = [ {
      "title": "US East (N. Virginia)",
      "latitude": 38.946728,
      "longitude": -77.443386,
      "color":colorSet.next()
    }, {
      "title": "US East (Ohio)",
      "latitude": 39.958587,
      "longitude": -82.997058,
      "color":colorSet.next()
    }, {
      "title": "US West (N. California)",
      "latitude": 37.758891,
      "longitude": -122.443318,
      "color":colorSet.next()
    }, {
      "title": "US West (Oregon)",
      "latitude": 45.841904,
      "longitude": -119.296774,
      "color":colorSet.next()
    }, {
      "title": "Africa (Cape Town)",
      "latitude": -33.953923,
      "longitude": 18.566379,
      "color":colorSet.next()
    }, {
      "title": "Asia Pacific (Mumbai)",
      "latitude": 19.085863,
      "longitude": 72.873766,
      "color":colorSet.next()
    }, {
      "title": "Asia Pacific (Seoul)",
      "latitude": 37.562049,
      "longitude": 127.007511,
      "color":colorSet.next()
    }, {
      "title": "Asia Pacific (Singapore)",
          "latitude": 1.353010,
          "longitude": 103.869377,
          "color":colorSet.next()
    },

      {
      "title": "Asia Pacific (Sydney)",
          "latitude": -33.831767,
          "longitude": 151.007401,
          "color":colorSet.next()
    }, {
      "title": "Asia Pacific (Tokyo)",
          "latitude": 35.688572,
          "longitude": 139.618912,
          "color":colorSet.next()
    }, {
      "title": "Canada (Central)",
          "latitude": 45.508968,
          "longitude": -73.616289,
          "color":colorSet.next()
    }, {
      "title": "Europe (Frankfurt)",
          "latitude": 50.110991,
          "longitude": 8.632203,
          "color":colorSet.next()
    }, {
      "title": "Europe (Ireland)",
        "latitude": 53.346174,
          "longitude": -6.272156,
          "color":colorSet.next()
    },{
      "title": "Europe (London)",
          "latitude": 51.516689,
          "longitude": -0.134100,
          "color":colorSet.next()
    },{
      "title": "Europe (Paris)",
          "latitude": 48.879382,
          "longitude": 2.341615,
          "color":colorSet.next()
    },{
      "title": "Middle East (Bahrain)",
          "latitude": 26.233356,
          "longitude": 50.585524,
          "color":colorSet.next()
    },{
      "title": "South America (São Paulo)",
          "latitude": -23.576129,
          "longitude": -46.614103,
          "color":colorSet.next()
    }



  ];

    /*imageSeries.data = [ {
      "title": "Europe",
      "latitude": 49.540486,
      "longitude": 9.881931,
      "color":colorSet.next()
    }, {
      "title": "North America",
      "latitude": 39.787092,
      "longitude": -99.754244,
      "color":colorSet.next()
    }, {
      "title": "South America",
      "latitude": -14.864205,
      "longitude": -55.902655,
      "color":colorSet.next()
    }, {
      "title": "Africa",
      "latitude": 4.256283,
      "longitude": 23.308534,
      "color":colorSet.next()
    }, {
      "title": "Middle East",
      "latitude": 31.971059,
      "longitude": 46.572714,
      "color":colorSet.next()
    }, {
      "title": "Asia",
      "latitude": 35.360601,
      "longitude": 95.728129,
      "color":colorSet.next()
    }, {
      "title": "Oceania",
      "latitude": -13.611172,
      "longitude": 130.755130,
      "color":colorSet.next()
    } ];*/


    return () => {

    };
  }, []);

  return (
    <div id="chartdiv" style={{ width: "100%", height: "750px" }}></div>
  );
}
export default App;