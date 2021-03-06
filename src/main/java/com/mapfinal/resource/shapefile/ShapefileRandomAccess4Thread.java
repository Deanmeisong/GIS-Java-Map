package com.mapfinal.resource.shapefile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.mapfinal.dispatcher.Dispatcher;
import com.mapfinal.dispatcher.FeatureDispatcher;
import com.mapfinal.dispatcher.SpatialIndexObject;
import com.mapfinal.dispatcher.SpatialIndexer;
import com.mapfinal.dispatcher.indexer.jts.KdTreeSpatialIndexer;
import com.mapfinal.dispatcher.indexer.jts.QuadtreeSpatialIndexer;
import com.mapfinal.dispatcher.indexer.jts.STRTreeSpatialIndexer;
import com.mapfinal.map.Feature;
import com.mapfinal.resource.ResourceDispatcher;
import com.mapfinal.resource.shapefile.dbf.MapRecordSet;
import com.mapfinal.resource.shapefile.shpx.ShpInfo;
import com.mapfinal.resource.shapefile.shpx.ShpPoint;
import com.mapfinal.resource.shapefile.shpx.ShpPointContent;
import com.mapfinal.resource.shapefile.shpx.ShpRandomAccess;
import com.mapfinal.resource.shapefile.shpx.ShpRecordHeader;
import com.mapfinal.resource.shapefile.shpx.ShpRecordRandomReader;
import com.mapfinal.resource.shapefile.shpx.ShpType;
import com.mapfinal.resource.shapefile.shpx.ShxRecord;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;

public class ShapefileRandomAccess4Thread {

	private ShapefileReaderFactory readerFactory;
	
	public ShapefileRandomAccess4Thread(ShapefileReaderFactory readerFactory) {
		// TODO Auto-generated constructor stub
		this.setReaderFactory(readerFactory);
	}
	
	public Dispatcher buildDispatcher(ResourceDispatcher resource, ShapefileRandomAccess randomAccess) throws IOException {
		// TODO Auto-generated method stub
		Dispatcher dispatchar = null;
		SpatialIndexer indexer = null;
		int shpType = randomAccess.getShpType();
		switch (shpType) {
		case ShpType.NULL_SHAPE:
			break;
		case ShpType.POINT:
			indexer = new KdTreeSpatialIndexer();
			dispatchar = new FeatureDispatcher(indexer, resource);
			buildKdTreeSpatialIndex(indexer, shpType, randomAccess);
			break;
		case ShpType.POLYLINE:
			indexer = new STRTreeSpatialIndexer();
			dispatchar = new FeatureDispatcher(indexer, resource);
			buildSpatialIndex(indexer, shpType, randomAccess);
			break;
		case ShpType.POLYGON:
			indexer = new QuadtreeSpatialIndexer();
			buildSpatialIndex(indexer, shpType, randomAccess);
			dispatchar = new FeatureDispatcher(indexer, resource);
			break;
		case ShpType.MULTIPOINT:
			dispatchar = new FeatureDispatcher(new KdTreeSpatialIndexer(), resource);
			break;
		case ShpType.POINT_Z:
			dispatchar = new FeatureDispatcher(new KdTreeSpatialIndexer(), resource);
			break;
		case ShpType.POLYLINE_Z:
			indexer = new QuadtreeSpatialIndexer();
			dispatchar = new FeatureDispatcher(indexer, resource);
			buildSpatialIndex(indexer, shpType, randomAccess);
			break;
		case ShpType.POLYGONZ:
			indexer = new QuadtreeSpatialIndexer();
			dispatchar = new FeatureDispatcher(indexer, resource);
			buildSpatialIndex(indexer, shpType, randomAccess);
			break;
		case ShpType.MULTIPOINT_Z:
			indexer = new STRTreeSpatialIndexer();
			dispatchar = new FeatureDispatcher(indexer, resource);
			buildSpatialIndex(indexer, shpType, randomAccess);
			break;
		case ShpType.POINT_M:
			dispatchar = new FeatureDispatcher(new KdTreeSpatialIndexer(), resource);
			break;
		case ShpType.POLYLINE_M:
			indexer = new STRTreeSpatialIndexer();
			dispatchar = new FeatureDispatcher(indexer, resource);
			buildSpatialIndex(indexer, shpType, randomAccess);
			break;
		case ShpType.POLYGON_M:
			indexer = new QuadtreeSpatialIndexer();
			dispatchar = new FeatureDispatcher(indexer, resource);
			buildSpatialIndex(indexer, shpType, randomAccess);
			break;
		case ShpType.MULTIPOINT_M:
			dispatchar = new FeatureDispatcher(new KdTreeSpatialIndexer(), resource);
			break;
		case ShpType.MULTIPATCH:
			indexer = new QuadtreeSpatialIndexer();
			dispatchar = new FeatureDispatcher(indexer, resource);
			buildSpatialIndex(indexer, shpType, randomAccess);
			break;
		}
		return dispatchar;
	}
	
	public void buildKdTreeSpatialIndex(SpatialIndexer indexer, int shpType, ShapefileRandomAccess randomAccess) throws IOException {
		ShpPointContent pointContent;
		int length = 0;
		// ???????????????,?????????1??????
		for (int i = 1; i <= randomAccess.getRecordCount(); i++) {
			ShxRecord shx = randomAccess.getShxRandomAccess().getRecordPosition(i);
			length = shx.length();
			if (length <= 0) {
				continue;
			}
			int offset = shx.offset();
			byte[] buf = new byte[length];
			randomAccess.getShpRandomAccess().readRecordBuf(buf, offset, length);
			ShpRecordRandomReader shpRecord = new ShpRecordRandomReader(buf, length);
			// ?????????????????????
			ShpRecordHeader shpRecordHeader = shpRecord.getRecordHeader();
			if (shpRecordHeader == null) {
				break;
			}
			// ???????????????????????????shp??????????????????,???????????????????????????????????????????????????
			if (shpRecordHeader.iContentLength * 2 != ShpPointContent.SIZE
					|| shpRecordHeader.iContentLength * 2 != length) {
				break;
			}
			byte[] ptBytesContent = new byte[ShpPointContent.SIZE];
			shpRecord.read(ptBytesContent, 0, ptBytesContent.length);
			pointContent = new ShpPointContent(ptBytesContent);
			// ??????????????????
			if (pointContent.iShpType != randomAccess.getShpType()) {
				break;
			}
			Coordinate p =new Coordinate(pointContent.x, pointContent.y);
			String id = String.valueOf(i-1);
			String dataType = "shp";
			String geometryType = "POINT";
			Envelope env = new Envelope(p);
			SpatialIndexObject sio = new SpatialIndexObject(id, dataType, geometryType, env);
			((KdTreeSpatialIndexer)indexer).insert(p, sio);
		}
	}
	
	/**
	 * ????????????
	 * @param indexer
	 * @param shpType
	 * @param shxRandomAccess
	 * @throws IOException
	 */
	public void buildSpatialIndex(SpatialIndexer indexer, int shpType, ShapefileRandomAccess randomAccess) throws IOException {
		System.out.println("[ShpRandomAccess] buildSpatialIndex");
		int length = 0;
		for (int i = 1; i <= randomAccess.getRecordCount(); i++) {
			ShxRecord shx = randomAccess.getShxRandomAccess().getRecordPosition(i);
			//System.out.println("ShxRecord: " + shx.toString());
			length = shx.length();
			if (length <= 0) {
				continue;
			}
			int offset = shx.offset();
			byte[] buf = new byte[length];
			randomAccess.getShpRandomAccess().readRecordBuf(buf, offset, length);
			ShpRecordRandomReader shpRecord = new ShpRecordRandomReader(buf, length);
			// ???????????????
			ShpRecordHeader shpRecordHeader = shpRecord.getRecordHeader();
			if (shpRecordHeader == null) {
				continue;
			}
			ShpInfo shpInfo = null;
			try {
				shpInfo = shpRecord.getShpInfo();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (shpInfo == null) {
				continue;
			}
			// ???????????????
			if (shpInfo.iShpType != shpType) {
				continue;
			}
			if (shpInfo.iNumParts * 4 > ShpRecordRandomReader.MAX_BUFFER_SIZE) {
				continue;
			}
			// ??????????????????????????????
			int actualLength = ShpInfo.SIZE + shpInfo.iNumParts * 4;
			actualLength += shpInfo.iNumPoints * ShpPoint.SIZE;
			// ??????????????????????????????????????????????????????
			if (shpRecordHeader.iContentLength * 2 != actualLength) {
				continue;
			}
			// ??????shp????????????
			String id = String.valueOf(i-1);
			String dataType = "shp";
			String geometryType = ShpType.shpTypeName(shpType);
			Envelope env = new Envelope(shpInfo.ptBox[0].x, shpInfo.ptBox[1].x, shpInfo.ptBox[0].y, shpInfo.ptBox[1].y);
			//System.out.println("sio env: " + env.toString());
			SpatialIndexObject sio = new SpatialIndexObject(id, dataType, geometryType, env);
			if(indexer instanceof QuadtreeSpatialIndexer) {
				QuadtreeSpatialIndexer qsi = (QuadtreeSpatialIndexer) indexer;
				qsi.insert(sio.getEnvelope(), sio);
			} else if(indexer instanceof STRTreeSpatialIndexer) {
				STRTreeSpatialIndexer ssi = (STRTreeSpatialIndexer) indexer;
				ssi.insert(sio.getEnvelope(), sio);
			} else {
				continue;
			}
		}
	}
	
	public List<Object> readRecordAll(ShapefileRandomAccess randomAccess) throws IOException {
		List<Object> points = new ArrayList<Object>();
		for (int i = 1; i <= randomAccess.getRecordCount(); i++) {
			ShxRecord shx = randomAccess.getShxRandomAccess().getRecordPosition(i);
			ShpRandomAccess shp = randomAccess.getShpRandomAccess();
			int shpType = randomAccess.getShpType();
			switch (shpType) {
			case ShpType.NULL_SHAPE:
				break;
			case ShpType.POINT: /* ????????? */
				Point pt = readRecordPoint(shx, shp, shpType);
				pt.setUserData(i-1);
				points.add(pt);
				break;
			case ShpType.POLYLINE: /* ????????? */
				LineString line = readRecordPolyline(shx, shp, shpType);
				line.setUserData(i-1);
				points.add(line);
				break;
			case ShpType.POLYGON:/* ????????? */
				MultiPolygon polygon = readRecordPolygon(shx, shp, shpType);
				polygon.setUserData(i-1);
				points.add(polygon);
				break;
			case ShpType.MULTIPOINT:
				break;
			case ShpType.POINT_Z:
				break;
			case ShpType.POLYLINE_Z:
				break;
			case ShpType.POLYGONZ:
				break;
			case ShpType.MULTIPOINT_Z:
				break;
			case ShpType.POINT_M:
				break;
			case ShpType.POLYLINE_M:
				break;
			case ShpType.POLYGON_M:
				break;
			case ShpType.MULTIPOINT_M:
				break;
			case ShpType.MULTIPATCH:
				break;
			}
		}
		return points;
	}
	
	public Feature readRecord(SpatialIndexObject obj, ShapefileRandomAccess randomAccess) throws IOException {
		Integer i = Integer.valueOf(obj.getId());
		//System.out.println("[shp_thread] sio: " + obj.getId() + ", recordCount: " + randomAccess.getRecordCount());
		if(i==null || i <0 || i>randomAccess.getRecordCount()) return null;
		ShxRecord shx = randomAccess.getShxRandomAccess().getRecordPosition(i+1);
		//System.out.println("[shp_thread] shxRecord: " + shx.toString());
		ShpRandomAccess shp = randomAccess.getShpRandomAccess();
		MapRecordSet recordSet = randomAccess.getRecordSet();
		int shpType = randomAccess.getShpType();
		switch (shpType) {
		case ShpType.NULL_SHAPE:
			break;
		case ShpType.POINT: /* ????????? */
			return readerFactory.readRecordPoint(recordSet, obj);
		case ShpType.POLYLINE: /* ????????? */
			return readRecordPolyline(shx, shp, recordSet, obj);
		case ShpType.POLYGON:/* ????????? */
			return readRecordPolygon(shx, shp, recordSet, obj);
		case ShpType.MULTIPOINT:
			break;
		case ShpType.POINT_Z:
			break;
		case ShpType.POLYLINE_Z:
			break;
		case ShpType.POLYGONZ:
			break;
		case ShpType.MULTIPOINT_Z:
			break;
		case ShpType.POINT_M:
			break;
		case ShpType.POLYLINE_M:
			break;
		case ShpType.POLYGON_M:
			break;
		case ShpType.MULTIPOINT_M:
			break;
		case ShpType.MULTIPATCH:
			break;
		}
		return null;
	}
	
	/**
	 * ????????????Point??????
	 * @param shx
	 * @return
	 * @throws IOException
	 */
	public Point readRecordPoint(ShxRecord shx, ShpRandomAccess shp, int shpType) throws IOException {
		int length = shx.length();
		if (length <= 0) {
			return null;
		}
		int offset = shx.offset();
		byte[] buf = new byte[length];
		shp.readRecordBuf(buf, offset, length);
		ShpRecordRandomReader shpRecord = new ShpRecordRandomReader(buf, length);
		return readerFactory.readRecordPoint(shpRecord);
	}
	
	/**
	 * ????????????ShpType.POLYLINE??????
	 * @param shx
	 * @return
	 * @throws IOException
	 */
	public LineString readRecordPolyline(ShxRecord shx, ShpRandomAccess shp, int shpType) throws IOException {
		int length = shx.length();
		if (length <= 0) {
			return null;
		}
		int offset = shx.offset();
		byte[] buf = new byte[length];
		shp.readRecordBuf(buf, offset, length);
		ShpRecordRandomReader shpRecord = new ShpRecordRandomReader(buf, length);
		return readerFactory.readRecordPolyline(shpRecord);
	}
	
	/**
	 * ????????????ShpType.POLYGON??????
	 * @param shx
	 * @return
	 * @throws IOException
	 */
	public MultiPolygon readRecordPolygon(ShxRecord shx, ShpRandomAccess shp, int shpType) throws IOException {
		//long start = System.currentTimeMillis();
		// ???????????????i??????
		int length = shx.length();
		if (length <= 0) {
			System.out.print("polygon_01 & ");
			return null;
		}
		int offset = shx.offset();
		byte[] buf = new byte[length];
		shp.readRecordBuf(buf, offset, length);
		ShpRecordRandomReader shpRecord = new ShpRecordRandomReader(buf, length);
		return readerFactory.readRecordPolygon(shpRecord);
	}
	
	
	/**
	 * ????????????Point??????
	 * @param shx
	 * @return
	 * @throws IOException
	 */
	public Feature readRecordPoint(ShxRecord shx, ShpRandomAccess shp, MapRecordSet recordSet, SpatialIndexObject sio) throws IOException {
		int length = shx.length();
		if (length <= 0) {
			return null;
		}
		int offset = shx.offset();
		byte[] buf = new byte[length];
		shp.readRecordBuf(buf, offset, length);
		ShpRecordRandomReader shpRecord = new ShpRecordRandomReader(buf, length);
		return readerFactory.readRecordPoint(shpRecord, recordSet, sio);
	}
	
	/**
	 * ????????????ShpType.POLYLINE??????
	 * @param shx
	 * @return
	 * @throws IOException
	 */
	public Feature readRecordPolyline(ShxRecord shx, ShpRandomAccess shp, MapRecordSet recordSet, SpatialIndexObject sio) throws IOException {
		int length = shx.length();
		if (length <= 0) {
			return null;
		}
		int offset = shx.offset();
		byte[] buf = new byte[length];
		shp.readRecordBuf(buf, offset, length);
		ShpRecordRandomReader shpRecord = new ShpRecordRandomReader(buf, length);
		return readerFactory.readRecordPolyline(shpRecord, recordSet, sio);
	}
	
	/**
	 * ????????????ShpType.POLYGON??????
	 * @param shx
	 * @return
	 * @throws IOException
	 */
	public Feature readRecordPolygon(ShxRecord shx, ShpRandomAccess shp, MapRecordSet recordSet, SpatialIndexObject sio) throws IOException {
		//long start = System.currentTimeMillis();
		// ???????????????i??????
		int length = shx.length();
		if (length <= 0) {
			System.out.print("polygon_01 & ");
			return null;
		}
		int offset = shx.offset();
		byte[] buf = new byte[length];
		shp.readRecordBuf(buf, offset, length);
		ShpRecordRandomReader shpRecord = new ShpRecordRandomReader(buf, length);
		return readerFactory.readRecordPolygon(shpRecord, recordSet, sio);
	}

	public ShapefileReaderFactory getReaderFactory() {
		return readerFactory;
	}

	public void setReaderFactory(ShapefileReaderFactory readerFactory) {
		this.readerFactory = readerFactory;
	}

}
