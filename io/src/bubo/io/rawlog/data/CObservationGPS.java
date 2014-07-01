/*
 * Copyright (c) 2013-2014, Peter Abeles. All Rights Reserved.
 *
 * This file is part of Project BUBO.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package bubo.io.rawlog.data;

import bubo.io.LittleEndianIO;
import bubo.io.rawlog.RawlogDecoder;
import bubo.io.rawlog.RawlogSerializableCustom;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Rawlog class for GPS observations.
 *
 * @author Peter Abeles
 */
public class CObservationGPS extends CObservation implements RawlogSerializableCustom {

	public TGPSDatum_GGA GGA_datum = new TGPSDatum_GGA(); // If "has_GGA_datum" is true, this contains the read GGA datum.
	public TGPSDatum_RMC RMC_datum = new TGPSDatum_RMC(); // If "has_RMC_datum" is true, this contains the read RMC datum.
	public TGPSDatum_PZS PZS_datum = new TGPSDatum_PZS(); // If "has_PZS_datum" is true, this contains the read PZS datum (TopCon's mmGPS devices only)
	public TGPSDatum_SATS SATS_datum = new TGPSDatum_SATS(); // If "has_SATS_datum" is true, this contains the read PZS datum (TopCon's mmGPS devices only)
	/**
	 * Will be true if the corresponding field contains data read from the sensor, or false if it is not available.
	 * \sa GGA_datum
	 */
	boolean has_GGA_datum;
	/**
	 * Will be true if the corresponding field contains data read from the sensor, or false if it is not available.
	 * \sa RMC_datum
	 */
	boolean has_RMC_datum;
	/**
	 * Will be true if the corresponding field contains data read from the sensor, or false if it is not available.
	 * \sa PZS_datum
	 */
	boolean has_PZS_datum;
	/**
	 * Will be true if the corresponding field contains data read from the sensor, or false if it is not available.
	 * \sa SATS_datum
	 */
	boolean has_SATS_datum;
	// Sensor of the GPS on the robot
	private CPose3D sensorPose;

	// simulates a raw read of memory from the original data structure
	public static void readDatumRaw(TGPSDatum_GGA datum, RawlogDecoder decoder) throws IOException {
		datum.UTCTime.hour = (byte) LittleEndianIO.readByte(decoder.getInput());
		datum.UTCTime.minute = (byte) LittleEndianIO.readByte(decoder.getInput());
		datum.UTCTime.sec = LittleEndianIO.readDouble(decoder.getInput());

		datum.latitude_degrees = LittleEndianIO.readDouble(decoder.getInput());
		datum.longitude_degrees = LittleEndianIO.readDouble(decoder.getInput());
		datum.fix_quality = (byte) LittleEndianIO.readByte(decoder.getInput());
		datum.altitude_meters = LittleEndianIO.readDouble(decoder.getInput());
		datum.satellitesUsed = LittleEndianIO.readInt(decoder.getInput());
		datum.thereis_HDOP = LittleEndianIO.readBoolean(decoder.getInput());
		datum.HDOP = LittleEndianIO.readFloat(decoder.getInput());
	}

	public static void readDatumRaw(TGPSDatum_RMC datum, RawlogDecoder decoder) throws IOException {
		datum.UTCTime.hour = (byte) LittleEndianIO.readByte(decoder.getInput());
		datum.UTCTime.minute = (byte) LittleEndianIO.readByte(decoder.getInput());
		datum.UTCTime.sec = LittleEndianIO.readDouble(decoder.getInput());

		datum.validity_char = (byte) LittleEndianIO.readByte(decoder.getInput());
		datum.latitude_degrees = LittleEndianIO.readDouble(decoder.getInput());
		datum.longitude_degrees = LittleEndianIO.readDouble(decoder.getInput());
		datum.speed_knots = LittleEndianIO.readDouble(decoder.getInput());
		datum.direction_degrees = LittleEndianIO.readDouble(decoder.getInput());
	}

	public static void readDataum(TGPSDatum_PZS datum, RawlogDecoder decoder, int version) throws IOException {
		datum.latitude_degrees = LittleEndianIO.readDouble(decoder.getInput());
		datum.longitude_degrees = LittleEndianIO.readDouble(decoder.getInput());
		datum.height_meters = LittleEndianIO.readDouble(decoder.getInput());
		datum.RTK_height_meters = LittleEndianIO.readDouble(decoder.getInput());
		datum.PSigma = LittleEndianIO.readFloat(decoder.getInput());
		datum.angle_transmitter = LittleEndianIO.readDouble(decoder.getInput());
		datum.nId = (byte) LittleEndianIO.readByte(decoder.getInput());
		datum.Fix = (byte) LittleEndianIO.readByte(decoder.getInput());
		datum.TXBattery = (byte) LittleEndianIO.readByte(decoder.getInput());
		datum.RXBattery = (byte) LittleEndianIO.readByte(decoder.getInput());
		datum.error = (byte) LittleEndianIO.readByte(decoder.getInput());

		// extra data?
		if (version >= 6) {
			datum.hasCartesianPosVel = LittleEndianIO.readBoolean(decoder.getInput());
			datum.cartesian_x = LittleEndianIO.readDouble(decoder.getInput());
			datum.cartesian_y = LittleEndianIO.readDouble(decoder.getInput());
			datum.cartesian_z = LittleEndianIO.readDouble(decoder.getInput());
			datum.cartesian_vx = LittleEndianIO.readDouble(decoder.getInput());
			datum.cartesian_vy = LittleEndianIO.readDouble(decoder.getInput());
			datum.cartesian_vz = LittleEndianIO.readDouble(decoder.getInput());
			datum.hasPosCov = LittleEndianIO.readBoolean(decoder.getInput());
			datum.pos_covariance = (CMatrix) decoder.decodeObject();
			datum.hasVelCov = LittleEndianIO.readBoolean(decoder.getInput());
			datum.vel_covariance = (CMatrix) decoder.decodeObject();
			datum.hasStats = LittleEndianIO.readBoolean(decoder.getInput());
			datum.stats_GPS_sats_used = (byte) LittleEndianIO.readByte(decoder.getInput());
			datum.stats_GLONASS_sats_used = (byte) LittleEndianIO.readByte(decoder.getInput());

			if (version >= 8)
				datum.stats_rtk_fix_progress = (byte) LittleEndianIO.readByte(decoder.getInput());
			else
				datum.stats_rtk_fix_progress = 0;
		} else {
			datum.hasCartesianPosVel = false;
			datum.hasPosCov = false;
			datum.hasVelCov = false;
			datum.hasStats = false;
		}
	}

	@Override
	public void customDecoding(int version, RawlogDecoder decoder) {
		try {
			switch (version) {
				case 0: {
					has_GGA_datum = LittleEndianIO.readBoolean(decoder.getInput());
					if (has_GGA_datum)
						readDatumRaw(GGA_datum, decoder);

					has_RMC_datum = LittleEndianIO.readBoolean(decoder.getInput());
					if (has_RMC_datum)
						readDatumRaw(RMC_datum, decoder);
				}
				break;
				case 1:
				case 2:
				case 3:
				case 4:
				case 5:
				case 6:
				case 7:
				case 8: {
					if (version >= 3)
						setTimestamp(LittleEndianIO.readLong(decoder.getInput()));
					else
						setTimestamp(0);

					has_GGA_datum = LittleEndianIO.readBoolean(decoder.getInput());
					if (has_GGA_datum) {
						readDatumRaw(GGA_datum, decoder);
					}

					has_RMC_datum = LittleEndianIO.readBoolean(decoder.getInput());
					if (has_RMC_datum) {
						readDatumRaw(RMC_datum, decoder);
					}

					if (version > 1)
						setSensorLabel(decoder.readString());
					else
						setSensorLabel("");

					if (version >= 4)
						sensorPose = (CPose3D) decoder.decodeObject();
					else
						sensorPose = new CPose3D();

					if (version >= 5) {
						has_PZS_datum = LittleEndianIO.readBoolean(decoder.getInput());
						if (has_PZS_datum) {
							readDataum(PZS_datum, decoder, version);
						}
					} else {
						has_PZS_datum = false;
					}

					// Added in V7:
					if (version >= 7) {
						has_SATS_datum = LittleEndianIO.readBoolean(decoder.getInput());
						if (has_SATS_datum) {

							SATS_datum.USIs = decoder.readByteArray();
							SATS_datum.ELs = decoder.readByteArray();
							SATS_datum.AZs = decoder.readByteArray();
						}
					} else has_SATS_datum = false;

				}
				break;

				default:
					throw new RuntimeException("Unknown version " + version);

			}

		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void customEncoding(OutputStream output) {
		//To change body of implemented methods use File | Settings | File Templates.
	}

	public CPose3D getSensorPose() {
		return sensorPose;
	}

	public void setSensorPose(CPose3D sensorPose) {
		this.sensorPose = sensorPose;
	}

	public boolean isHas_GGA_datum() {
		return has_GGA_datum;
	}

	public void setHas_GGA_datum(boolean has_GGA_datum) {
		this.has_GGA_datum = has_GGA_datum;
	}

	public boolean isHas_RMC_datum() {
		return has_RMC_datum;
	}

	public void setHas_RMC_datum(boolean has_RMC_datum) {
		this.has_RMC_datum = has_RMC_datum;
	}

	public boolean isHas_PZS_datum() {
		return has_PZS_datum;
	}

	public void setHas_PZS_datum(boolean has_PZS_datum) {
		this.has_PZS_datum = has_PZS_datum;
	}

	public boolean isHas_SATS_datum() {
		return has_SATS_datum;
	}

	public void setHas_SATS_datum(boolean has_SATS_datum) {
		this.has_SATS_datum = has_SATS_datum;
	}

	@Override
	public int getVersion() {
		return 8;
	}

	/**
	 * A UTC time-stamp structure for GPS messages
	 */
	public static class TUTCTime {
		byte hour;
		byte minute;
		double sec;
		public TUTCTime() {
		}

		public boolean isEqual(TUTCTime a) {
			return a.hour == hour && a.minute == minute && a.sec == sec;
		}
	}

	/**
	 * The GPS datum for GGA commands
	 */
	public static class TGPSDatum_GGA {
		/**
		 * The GPS sensor measured timestamp (in UTC time)
		 */
		public TUTCTime UTCTime = new TUTCTime();
		/**
		 * The measured latitude, in degrees (North:+ , South:-)
		 */
		public double latitude_degrees;
		/**
		 * The measured longitude, in degrees (East:+ , West:-)
		 */
		public double longitude_degrees;
		/**
		 * The values defined in the NMEA standard are the following:
		 * <p/>
		 * 0 = invalid
		 * 1 = GPS fix (SPS)
		 * 2 = DGPS fix
		 * 3 = PPS fix
		 * 4 = Real Time Kinematic
		 * 5 = Float RTK
		 * 6 = estimated (dead reckoning) (2.3 feature)
		 * 7 = Manual input mode
		 * 8 = Simulation mode
		 */
		public byte fix_quality;
		/**
		 * The measured altitude, in meters.
		 */
		public double altitude_meters;
		/**
		 * The number of satelites used to compute this estimation.
		 */
		public int satellitesUsed;
		/**
		 * This states whether to take into account the value in the HDOP field.
		 */
		public boolean thereis_HDOP;
		/**
		 * The HDOP (Horizontal Dilution of Precision) as returned by the sensor.
		 */
		public float HDOP;

		public TGPSDatum_GGA() {
		}
	}

	/**
	 * The GPS datum for RMC commands
	 */
	public static class TGPSDatum_RMC {
		/**
		 * The GPS sensor measured timestamp (in UTC time)
		 */
		TUTCTime UTCTime = new TUTCTime();
		/**
		 * This will be: 'A'=OK or 'V'=void
		 */
		byte validity_char;
		/**
		 * The measured latitude, in degrees (North:+ , South:-)
		 */
		double latitude_degrees;
		/**
		 * The measured longitude, in degrees (East:+ , West:-)
		 */
		double longitude_degrees;
		/**
		 * The measured speed (in knots)
		 */
		double speed_knots;
		/**
		 * The measured speed direction (in degrees)
		 */
		double direction_degrees;

		TGPSDatum_RMC() {

		}
	}

	/**
	 * The GPS datum for TopCon's mmGPS devices
	 */
	public static class TGPSDatum_PZS {
		double latitude_degrees;    //!< The measured latitude, in degrees (North:+ , South:-)
		double longitude_degrees;    //!< The measured longitude, in degrees (East:+ , West:-)
		double height_meters;        //!< ellipsoidal height from N-beam [m] perhaps weighted with regular gps
		double RTK_height_meters;    //!< ellipsoidal height [m] without N-beam correction
		float PSigma;                //!< position SEP [m]
		double angle_transmitter;    //!< Vertical angle of N-beam
		byte nId;        //!< ID of the transmitter [1-4], 0 if none.
		byte Fix;        //!< 1: GPS, 2: mmGPS
		byte TXBattery;    //!< battery level on transmitter
		byte RXBattery;    //!< battery level on receiver
		byte error;        //! system error indicator
		boolean hasCartesianPosVel;
		double cartesian_x, cartesian_y, cartesian_z;  //!< Only if hasCartesianPosVel is true
		double cartesian_vx, cartesian_vy, cartesian_vz;  //!< Only if hasCartesianPosVel is true
		boolean hasPosCov;
		CMatrix pos_covariance;    // 4 by 4 matrix: Only if hasPosCov is true
		boolean hasVelCov;
		CMatrix vel_covariance;    // 4 by 4 matrix: Only if hasPosCov is true
		boolean hasStats;
		byte stats_GPS_sats_used, stats_GLONASS_sats_used; //<! Only if hasStats is true
		byte stats_rtk_fix_progress; //!< [0,100] %, only in modes other than RTK FIXED.
		TGPSDatum_PZS() {
		}

	}


	/**
	 * A generic structure for statistics about tracked satelites and their positions.
	 */
	public static class TGPSDatum_SATS {
		byte[] USIs;  //unsigned: The list of USI (Universal Sat ID) for the detected sats (See GRIL Manual, pag 4-31).
		byte[] ELs; //signed:  Elevation (in degrees, 0-90) for each satellite in USIs.
		byte[] AZs; //singed:< Azimuth (in degrees, 0-360) for each satellite in USIs.
		TGPSDatum_SATS() {
		}
	}
}
