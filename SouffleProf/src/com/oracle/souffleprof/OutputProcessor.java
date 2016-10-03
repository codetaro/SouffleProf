package com.oracle.souffleprof;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class OutputProcessor {
	
	private ProgramRun programRun;

	
	public OutputProcessor() {
		programRun = new ProgramRun();
	}

	/*
	 * rel table : 
	 * ROW[0] = TOT_T 
	 * ROW[1] = NREC_T 
	 * ROW[2] = REC_T 
	 * ROW[3] = COPY_T
	 * ROW[4] = TUPLES 
	 * ROW[5] = REL NAME 
	 * ROW[6] = ID 
	 * ROW[7] = SRC 
	 * ROW[8] = PERFOR
	 * 
	 */
	public Object[][] getRelTable() {
		Map<String, Relation> relation_map = programRun.getRelation_map();
		
		Object[][] table = new Object[relation_map.size()][];
		int i = 0;
		for (Relation r : relation_map.values()) {
			Object[] row = new Object[9];
			row[0] = r.getNonRecTime() + r.getRecTime() + r.getCopyTime();
			row[1] = r.getNonRecTime();
			row[2] = r.getRecTime();
			row[3] = r.getCopyTime();
			row[4] = r.getNum_tuplesRel();
			row[5] = r.getName();
			row[6] = r.getId();
			row[7] = r.getLocator();
			if ((Double) row[0] != 0.0) {
				row[8] = (Double) ((Long) row[4] / (Double) row[0]);
			} else {
				row[8] = (Double) ((Long) row[4] / 1.0);
			}
			table[i++] = row;
		}
		return table;
	}
	
	/*
	 * rul table : 
	 * ROW[0] = TOT_T 
	 * ROW[1] = NREC_T 
	 * ROW[2] = REC_T 
	 * ROW[3] = COPY_T
	 * ROW[4] = TUPLES 
	 * ROW[5] = RUL NAME 
	 * ROW[6] = ID 
	 * ROW[7] = SRC 
	 * ROW[8] = PERFOR 
	 * ROW[9] = VER 
	 * ROW[10]= REL_NAME
	 */
	public Object[][] getRulTableGui() {
		Map<String, Relation> relation_map = programRun.getRelation_map();
		Double tot_copy_time = programRun.getTotCopyTime();
		Double tot_rec_tup = programRun.getTotNumRecTuples().doubleValue();;
		
		Map<String, Object[]> rule_map = new HashMap<String, Object[]>();

		for (Relation rel : relation_map.values()) {
			for (Rule rul : rel.getRuleMap().values()) {
				Object[] temp = new Object[11];
				temp[1] = rul.getRuntime();
				temp[2] = 0.0;
				temp[3] = 0.0;
				temp[4] = rul.getNum_tuples();
				temp[5] = rul.getName();
				temp[6] = rul.getId();
				temp[7] = rul.getLocator();
				temp[9] = 0;
				temp[10] = rel.getName();

				rule_map.put(rul.getName(), temp);
			}

			for (Iteration iter : rel.getIterations()) {
				Object[] temp;
				for (RuleRecursive rul : iter.getRul_rec().values()) {

					if (rule_map.containsKey(rul.getName())) {
						temp = rule_map.get(rul.getName());
						temp[2] = (Double) temp[2] + (Double) rul.getRuntime();
						temp[4] = (Long) temp[4] + rul.getNum_tuples();
					} else {
						temp = new Object[11];
						temp[1] = 0.0;
						temp[2] = (Double) rul.getRuntime();
						temp[3] = 0.0;
						temp[4] = (Long) rul.getNum_tuples();
						temp[6] = rul.getId();
						temp[5] = rul.getName();
						temp[9] = rul.getVersion();
						temp[10] = rel.getName();

					}
					temp[0] = rul.getRuntime();
					rule_map.put(rul.getName(), temp);
				}
			}
			for (Object[] t : rule_map.values()) {
				if (((String) t[6]).charAt(0) == 'C') {

					Long d2 = (Long) t[4];
					Double rec_tup = d2.doubleValue();
					t[3] = (tot_copy_time / (Double) tot_rec_tup) * rec_tup;
				}
				t[0] = (Double) t[1] + (Double) t[2] + (Double) t[3];

				if ((Double) t[0] != 0.0) {
					t[8] = (Double) ((Long) t[4] / (Double) t[0]);
				} else {
					t[8] = (Double) ((Long) t[4] / 1.0);
				}
			}
		}
		Object[][] table = new Object[rule_map.size()][];
		int i = 0;
		for (Object[] row : rule_map.values()) {
			table[i++] = row;
		}
		return table;
	}
	
	/*
	 * rul table : 
	 * ROW[0] = TOT_T 
	 * ROW[1] = NREC_T 
	 * ROW[2] = REC_T 
	 * ROW[3] = COPY_T
	 * ROW[4] = TUPLES 
	 * ROW[5] = RUL NAME 
	 * ROW[6] = ID 
	 * ROW[7] = REL_NAME 
	 * ROW[8] = VER 
	 * ROW[9] = PERFOR 
	 * ROW[10]= SRC
	 */
	public Object[][] getRulTable() {
		Map<String, Relation> relation_map = programRun.getRelation_map();
		
		Map<String, Object[]> rule_map = new HashMap<String, Object[]>();
		Double tot_rec_tup = programRun.getTotNumRecTuples().doubleValue();
		Double tot_copy_time = programRun.getTotCopyTime();

		for (Relation rel : relation_map.values()) {
			for (Rule rul : rel.getRuleMap().values()) {
				Object[] temp = new Object[11];
				temp[1] = rul.getRuntime();
				temp[2] = 0.0;
				temp[3] = 0.0;
				temp[4] = rul.getNum_tuples();
				temp[5] = rul.getName();
				temp[6] = rul.getId();
				temp[7] = rel.getName();
				temp[8] = 0;
				temp[10] = rul.getLocator();
				rule_map.put(rul.getName(), temp);
			}

			for (Iteration iter : rel.getIterations()) {
				Object[] temp;
				for (RuleRecursive rul : iter.getRul_rec().values()) {

					if (rule_map.containsKey(rul.getName())) {
						temp = rule_map.get(rul.getName());
						temp[2] = (Double) temp[2] + (Double) rul.getRuntime();
						temp[4] = (Long) temp[4] + rul.getNum_tuples();
					} else {
						temp = new Object[11];
						temp[1] = 0.0;
						temp[2] = (Double) rul.getRuntime();
						temp[3] = 0.0;
						temp[4] = (Long) rul.getNum_tuples();
						temp[6] = rul.getId();
						temp[5] = rul.getName();
						temp[7] = rel.getName();
						temp[8] = rul.getVersion();
					}
					temp[0] = rul.getRuntime();
					rule_map.put(rul.getName(), temp);
				}
			}
			for (Object[] t : rule_map.values()) {
				if (((String) t[6]).charAt(0) == 'C') {
					Long d2 = (Long) t[4];
					Double rec_tup = d2.doubleValue();
					t[3] = (tot_copy_time / (Double) tot_rec_tup) * rec_tup;
				}
				t[0] = (Double) t[1] + (Double) t[2] + (Double) t[3];
				if ((Double) t[0] != 0.0) {
					t[9] = (Double) ((Long) t[4] / (Double) t[0]);
				} else {
					t[9] = (Double) ((Long) t[4] / 1.0);
				}
			}
		}
		Object[][] table = new Object[rule_map.size()][];
		int i = 0;
		for (Object[] row : rule_map.values()) {
			table[i++] = row;
		}
		return table;
	}
	
	/*
	 * ver table : 
	 * ROW[0] = TOT_T 
	 * ROW[1] = NREC_T 
	 * ROW[2] = REC_T 
	 * ROW[3] = COPY_T
	 * ROW[4] = TUPLES 
	 * ROW[5] = RUL NAME 
	 * ROW[6] = ID 
	 * ROW[7] = SRC 
	 * ROW[8] = PERFOR 
	 * ROW[9] = VER 
	 * ROW[10]= REL_NAME
	 */
	public Object[][] getVersionsGui(String strRel, String strRul) {
		Map<String, Relation> relation_map = programRun.getRelation_map();
		Double tot_copy_time = programRun.getTotCopyTime();
		Double tot_rec_tup = programRun.getTotNumRecTuples().doubleValue();;
		
		Map<String, Object[]> rule_map = new HashMap<String, Object[]>();

		for (Relation rel : relation_map.values()) {
			if (rel.getId().equals(strRel)) {

				for (Iteration iter : rel.getIterations()) {
					Object[] temp;
					for (RuleRecursive rul : iter.getRul_rec().values()) {

						if (rul.getId().equals(strRul)) {
							String strTemp = rul.getName() + rul.getLocator() + rul.getVersion();
							if (rule_map.containsKey(strTemp)) {
								temp = rule_map.get(strTemp);
								temp[2] = (Double) temp[2] + (Double) rul.getRuntime();
								temp[4] = (Long) temp[4] + rul.getNum_tuples();

							} else {
								temp = new Object[11];
								temp[1] = 0.0;
								temp[2] = (Double) rul.getRuntime();
								temp[4] = (Long) rul.getNum_tuples();
								temp[5] = rul.getName();
								temp[6] = rul.getId();
								temp[7] = rul.getLocator();
								temp[8] = rul.getVersion();
								temp[10] = rel.getName();
							}
							temp[0] = rul.getRuntime();
							rule_map.put(strTemp, temp);
						}
					}
				}
				for (Object[] t : rule_map.values()) {
					Double d = tot_rec_tup;
					Long d2 = (Long) t[4];
					Double d3 = d2.doubleValue();
					t[3] = (tot_copy_time / (Double) d) * d3;
					t[0] = (Double) t[1] + (Double) t[2] + (Double) t[3];
					if ((Double) t[0] != 0.0) {
						t[9] = (Double) ((Long) t[4] / (Double) t[0]);
					} else {
						t[9] = (Double) ((Long) t[4] / 1.0);
					}
				}
				break;
			}

		}
		Object[][] table = new Object[rule_map.size()][];
		int i = 0;
		for (Object[] row : rule_map.values()) {
			table[i++] = row;
		}
		return table;
	}
	
	/*
	 * rul table : 
	 * ROW[0] = TOT_T 
	 * ROW[1] = NREC_T 
	 * ROW[2] = REC_T 
	 * ROW[3] = COPY_T
	 * ROW[4] = TUPLES 
	 * ROW[5] = RUL NAME 
	 * ROW[6] = ID 
	 * ROW[7] = REL_NAME 
	 * ROW[8] = VER 
	 * ROW[9] = LOCATOR
	 */
	public Object[][] getVersions(String strRel, String strRul) {
		Map<String, Relation> relation_map = programRun.getRelation_map();
		
		Map<String, Object[]> rule_map = new HashMap<String, Object[]>();
		Double tot_rec_tup = programRun.getTotNumRecTuples().doubleValue();
		Double tot_copy_time = programRun.getTotCopyTime();

		for (Relation rel : relation_map.values()) {
			if (rel.getId().equals(strRel)) {

				for (Iteration iter : rel.getIterations()) {
					Object[] temp;
					for (RuleRecursive rul : iter.getRul_rec().values()) {

						if (rul.getId().equals(strRul)) {
							String strTemp = rul.getName() + rul.getLocator() + rul.getVersion();
							if (rule_map.containsKey(strTemp)) {
								temp = rule_map.get(strTemp);
								temp[2] = (Double) temp[2] + (Double) rul.getRuntime();
								temp[4] = (Long) temp[4] + rul.getNum_tuples();

							} else {
								temp = new Object[10];
								temp[1] = 0.0;
								temp[2] = (Double) rul.getRuntime();
								temp[4] = (Long) rul.getNum_tuples();
								temp[5] = rul.getName();
								temp[6] = rul.getId();
								temp[7] = rel.getName();
								temp[8] = rul.getVersion();
								temp[9] = rul.getLocator();
							}
							temp[0] = rul.getRuntime();
							rule_map.put(strTemp, temp);
						}
					}
				}
				for (Object[] t : rule_map.values()) {
					Double d = tot_rec_tup;
					Long d2 = (Long) t[4];
					Double d3 = d2.doubleValue();
					t[3] = (tot_copy_time / (Double) d) * d3;
					t[0] = (Double) t[1] + (Double) t[2] + (Double) t[3];
				}
				break;
			}

		}
		Object[][] table = new Object[rule_map.size()][];
		int i = 0;
		for (Object[] row : rule_map.values()) {
			table[i++] = row;
		}
		return table;
	}
	
	public Object[][] formatTable(Object[][] table, int precision) {
		Object[][] new_table = new Object[table.length][table[0].length];
		for (int i = 0; i < table.length; i++) {
			for (int j = 0; j < table[0].length; j++) {
				if (j < 4 || j == table.length - 2) {
					new_table[i][j] = formatTime((Double) table[i][j]);
				} else if (j == 4) {
					new_table[i][4] = formatNum(precision, (Long) table[i][4]);
				} else {
					new_table[i][j] = table[i][j];
				}
			}
		}
        //        for (Object[] row : new_table) {
        //            row[0] = run.formatTime((Double)row[0]);
        //            row[1] = run.formatTime((Double)row[1]);
        //            row[2] = run.formatTime((Double)row[2]);
        //            row[3] = run.formatTime((Double)row[3]);
        //            row[4] = run.formatNum(precision, (Long)row[4]);
        //        }
		return new_table;
	}
	
	public String formatNum(int precision, Object number) {

		if (precision == -1) {
			return "" + number;
		}
		long amount = (Long) number;
		String result;
		DecimalFormat formatter;
		if (amount >= 1000000000) {
			amount = (amount + 5000000) / 10000000;
			result = amount + "B";
			result = result.substring(0, 1) + "." + result.substring(1);
		} else if (amount >= 100000000) {
			amount = (amount + 500000) / 1000000;
			result = amount + "M";
			formatter = new DecimalFormat("###,###");
		} else if (amount >= 1000000) {
			amount = (amount + 5000) / 10000;

			result = amount + "M";
			result = result.substring(0, result.length() - 3) + "."
					+ result.substring(result.length() - 3, result.length());
		} else {
			formatter = new DecimalFormat("###,###");
			result = formatter.format(amount);
		}

		return result;
	}
	
	public String formatTime(Object number) {
		long val;
		long sec = Math.round((Double) number);
		if (sec >= 100) {
			val = TimeUnit.SECONDS.toMinutes(sec);
			if (val < 100) {
				if (val < 10) {
					String temp = (double) ((double) (sec - (TimeUnit.MINUTES.toSeconds(val))) / 60) * 100 + "";
					return val + "." + temp.substring(0, 1) + "m";
				}
				return val + "m";
			}
			val = TimeUnit.MINUTES.toHours(val);
			if (val < 100) {
				return val + "h";
			}
			val = TimeUnit.HOURS.toDays(val);
			return val + "D";
		} else if (sec >= 10) {
			return sec + "";
		} else if (Double.compare((Double) number, 1.0) >= 0) {
			DecimalFormat formatter = new DecimalFormat("0.0");
			return formatter.format(number);
		}
		DecimalFormat formatter = new DecimalFormat(".000");
		return formatter.format(number);
	}

	public ProgramRun getProgramRun() {
		return programRun;
	}

	public void setProgramRun(ProgramRun programRun) {
		this.programRun = programRun;
	}
}
