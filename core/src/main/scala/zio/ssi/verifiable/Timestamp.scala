package zio.ssi.verifiable

import java.time.Instant
import java.util.Date

opaque type Timestamp = Instant

object Timestamp:
  def apply(value: Instant): Timestamp = value

  extension (timestamp: Timestamp)
    def toDate: Date = Date.from(timestamp)
