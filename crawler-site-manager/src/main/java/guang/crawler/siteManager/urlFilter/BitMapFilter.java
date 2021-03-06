package guang.crawler.siteManager.urlFilter;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.BitSet;

import org.apache.commons.codec.binary.Base64;

/**
 * 使用位图方式过滤所有的URL。可能会有误报的情况存在。
 *
 * @author yang
 *
 */
public class BitMapFilter implements ObjectFilter {
	public static BitMapFilter newFilter() throws NoSuchAlgorithmException {
		return new BitMapFilter();
	}
	
	/**
	 * 过滤器的位图的位的数量,共使用了2M内存
	 */
	private final int	  FILTER_SIZE	= 16 * 1024 * 1024;
	/**
	 * HASH的方法
	 */
	private final String	DIGESTALG	= "MD5";
	/**
	 * 位图
	 */
	private BitSet	      filterData;
	/**
	 * 对信息进行签名的工具,用来产生hash值
	 */
	private MessageDigest	digest;

	private BitMapFilter() throws NoSuchAlgorithmException {
		this.filterData = new BitSet(this.FILTER_SIZE);
		this.digest = MessageDigest.getInstance(this.DIGESTALG);
	}

	@Override
	public boolean contains(final Object object) {
		if (object == null) {
			return false;
		}
		int bitIdx = this.hashObject(object);
		synchronized (this.filterData) {
			return this.filterData.get(bitIdx);

		}
	}

	@Override
	public boolean containsAndSet(final Object object) {
		if (object == null) {
			return false;
		}
		int bitIdx = this.hashObject(object);
		synchronized (this.filterData) {
			if (this.filterData.get(bitIdx)) {
				return true;
			} else {
				this.filterData.set(bitIdx);
				return false;
			}

		}
	}

	@Override
	public void fromBackupString(final String dataString) {
		byte[] data = Base64.decodeBase64(dataString);
		this.filterData = BitSet.valueOf(data);
	}

	private int hashObject(final Object object) {
		byte[] digestData = this.digest.digest(object.toString()
		                                             .getBytes());
		int bitIdx = 0;
		bitIdx |= (digestData[7] & 0xff);
		bitIdx <<= 8;
		bitIdx |= (digestData[8] & 0xff);
		bitIdx <<= 8;
		bitIdx |= (digestData[9] & 0xff);
		return bitIdx;
	}

	@Override
	public String toBackupString() {
		byte[] data = this.filterData.toByteArray();

		String result = Base64.encodeBase64String(data);
		return result;
	}

}
