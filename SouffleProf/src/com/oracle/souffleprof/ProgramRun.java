/*
 * Souffle - A Datalog Compiler
 * Copyright (c) 2015, Oracle and/or its affiliates. All rights reserved
 * Licensed under the Universal Permissive License v 1.0 as shown at:
 * - https://opensource.org/licenses/UPL
 * - <souffle root>/licenses/SOUFFLE-UPL.txt
 */

package com.oracle.souffleprof;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * ProgramRun
 * 
 * Profile data model to represent a run of a Datalog program.
 * 
 */
public class ProgramRun implements Serializable{

    /**
     * 
     */
    private static final long serialVersionUID = -4236231035756069616L;
    private Double runtime;
    private Map<String, Relation> relation_map;
    private Double tot_rec_tup = 0.0;
    private Double tot_copy_time = 0.0;


    public ProgramRun() {
        relation_map = new HashMap<String, Relation>();
        runtime = -1.0;
    }

    public void update() {
        tot_rec_tup = getTotNumRecTuples().doubleValue();
        tot_copy_time = getTotCopyTime();
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("ProgramRun:" + runtime + "\nRelations:\n");
        for (Relation r : relation_map.values()) {
            result.append(r.toString() + "\n");
        }
        return result.toString();
    }

    public Map<String, Relation> getRelation_map() {
        return relation_map;
    }
    
    public void setRelation_map(Map<String, Relation> relation_map) {
    	this.relation_map = relation_map;
    }

    public String getRuntime() {
        if (runtime == -1.0) {
            return "--";
        }
        return formatTime(runtime)+"";
    }
    
    public void setRuntime(Double runtime) {
    	this.runtime = runtime;
    }

    public Long getTotNumTuples() {
        Long result = 0L;
        for (Relation r : relation_map.values()) {
            result += r.getTotNum_tuples();
        }
        return result;
    }

    public Long getTotNumRecTuples() {
        Long result = 0L;
        for (Relation r : relation_map.values()) {
            result += r.getTotNumRec_tuples();
        }
        return result;
    }

    public double getTotCopyTime() {
        double result = 0;
        for (Relation r : relation_map.values()) {
            result += r.getCopyTime();
        }
        return result;
    }

    public double getTotTime() {
        double result = 0;
        for (Relation r : relation_map.values()) {
            result += r.getRecTime() + r.getNonRecTime() + r.getCopyTime();
        }
        return  result;
    }

    public Relation getRelation(String name) {
        for (Relation rel : getRelation_map().values()) {
            if (rel.getName().equals(name)) {
                return rel;
            }
        }
        return null;
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
}
