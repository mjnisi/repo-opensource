package eu.trade.repo.model;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;


@Entity
@Table (name="score_view")
public class Score {

	@EmbeddedId
	private ScoreId scoreId;
	
	@Column(name="score", insertable=false, updatable=false)
	private BigDecimal score = BigDecimal.ZERO;
	
	public Score() {}
	
	public ScoreId getScoreId() {
		return scoreId;
	}
	public void setScoreId(ScoreId scoreId) {
		this.scoreId = scoreId;
	}

	public BigDecimal getScore() {
		return score;
	}
	public void setScore(BigDecimal score) {
		this.score = score;
	}
	
	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (scoreId == null ? 0 : scoreId.hashCode());
		return result;
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if ( !(obj instanceof Score) ) {
			return false;
		}

		Score other = (Score) obj;
		if (scoreId == null) {
			return false;
		}
		return scoreId.equals(other.scoreId);
	}

}