package test.mpp;
// adapted from https://github.com/synogen/mpp/blob/master/src/main/java/org/mppsolartest/serial/CRCUtil.java

public class CRCUtil {
	private static final char[] crc_tb = new char[] { '\u0000', 'အ', '⁂', 'っ', '䂄', '傥', '惆', '烧', '脈', '鄩', 'ꅊ', '녫',
		'소', '톭', '\ue1ce', '\uf1ef', 'ሱ', 'Ȑ', '㉳', '≒', '劵', '䊔', '狷', '拖', '錹', '茘', '덻', 'ꍚ', '펽', '쎜',
		'\uf3ff', '\ue3de', '③', '㑃', 'Р', 'ᐁ', '擦', '瓇', '䒤', '咅', 'ꕪ', '땋', '蔨', '锉', '\ue5ee', '\uf5cf', '얬',
		'햍', '㙓', '♲', 'ᘑ', 'ذ', '盗', '曶', '嚕', '䚴', '띛', 'ꝺ', '霙', '蜸', '\uf7df', '\ue7fe', '힝', '잼', '䣄', '壥',
		'梆', '碧', 'ࡀ', 'ᡡ', '⠂', '㠣', '짌', '\ud9ed', '\ue98e', '羚', '襈', '饩', 'ꤊ', '뤫', '嫵', '䫔', '窷', '檖', 'ᩱ',
		'\u0a50', '㨳', '⨒', '\udbfd', '쯜', '﮿', '\ueb9e', '魹', '識', '묻', '\uab1a', '沦', '粇', '䳤', '峅', 'Ⱒ', '㰃',
		'ౠ', '᱁', '\uedae', 'ﶏ', '췬', '\uddcd', '괪', '봋', '赨', '鵉', '纗', '溶', '廕', '仴', '㸓', '⸲', 'ṑ', '\u0e70',
		'ﾟ', '\uefbe', '\udfdd', '쿼', '뼛', '꼺', '齙', '轸', '醈', '膩', '뇊', 'ꇫ', '턌', '섭', '\uf14e', '\ue16f', 'ႀ',
		'¡', 'ヂ', '⃣', '倄', '䀥', '灆', '恧', '莹', '鎘', 'ꏻ', '돚', '쌽', '팜', '\ue37f', '\uf35e', 'ʱ', 'ነ', '⋳', '㋒',
		'䈵', '刔', '扷', '牖', '뗪', 'ꗋ', '閨', '薉', '\uf56e', '\ue54f', '픬', '씍', '㓢', 'Ⓝ', 'ᒠ', 'ҁ', '瑦', '摇', '吤',
		'䐅', '\ua7db', '럺', '螙', '鞸', '\ue75f', '\uf77e', '윝', '휼', '⛓', '㛲', 'ڑ', 'ᚰ', '晗', '癶', '䘕', '嘴',
		'\ud94c', '쥭', '癩', '\ue92f', '駈', '觩', '릊', 'ꦫ', '塄', '䡥', '砆', '栧', 'ᣀ', '࣡', '㢂', '⢣', '쭽', '\udb5c',
		'\ueb3f', 'ﬞ', '诹', '鯘', 'ꮻ', '뮚', '䩵', '婔', '樷', '稖', '૱', '\u1ad0', '⪳', '㪒', 'ﴮ', '\ued0f', '\udd6c',
		'쵍', '붪', '궋', '鷨', '跉', '簦', '氇', '層', '䱅', '㲢', 'ⲃ', '᳠', 'ು', '\uef1f', '＾', '콝', '\udf7c', '꾛', '뾺',
		'这', '鿸', '渗', '縶', '乕', '年', '⺓', '㺲', '໑', 'Ự' };

	public static boolean checkCRC(String resultValue) {
	        if (resultValue.length() <= 2)
			return false;
	        var firstValue = resultValue.substring(0, resultValue.length() - 2);
		var lastValue = resultValue.substring(resultValue.length() - 2);
		var pByte = firstValue.getBytes();
		var returnV = calculateCRC(pByte);
		var lastV = toHexString(lastValue);
		var reV = Integer.parseInt(lastV, 16);
		return reV == returnV;
	}

	public static byte[] getCRCByte(String command) {
		var crcint = calculateCRC(command.getBytes());
		var crclow = crcint & 255;
		var crchigh = crcint >> 8 & 255;
		return new byte[] { (byte) crchigh, (byte) crclow };
	}

	private static String toHexString(String s) {
		StringBuilder result = new StringBuilder();

		for (int i = 0; i < s.length(); ++i) {
			var ch = (short) s.charAt(i);
			if (ch < 0)
				ch = (short) (ch + 256);

			var chString = Integer.toHexString(ch);
			if (chString.length() < 2)
				chString = "0" + chString;

			result.append(chString);
		}

		return result.toString();
	}

	private static int calculateCRC(byte[] pByte) {
		try {
			int len = pByte.length;
			int i = 0;

			int crc;
			for (crc = 0; len-- != 0; ++i) {
				int da = 255 & (255 & crc >> 8) >> 4;
				crc <<= 4;
				crc ^= crc_tb[255 & (da ^ pByte[i] >> 4)];
				da = 255 & (255 & crc >> 8) >> 4;
				crc <<= 4;
				int temp = 255 & (da ^ pByte[i] & 15);
				crc ^= crc_tb[temp];
			}

			int bCRCLow = 255 & crc;
			int bCRCHign = 255 & crc >> 8;
			if (bCRCLow == 40 || bCRCLow == 13 || bCRCLow == 10)
				++bCRCLow;

			if (bCRCHign == 40 || bCRCHign == 13 || bCRCHign == 10)
				++bCRCHign;

			crc = (255 & bCRCHign) << 8;
			crc += bCRCLow;
			return crc;
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}
}
